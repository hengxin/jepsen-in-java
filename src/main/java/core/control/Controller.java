package core.control;

import core.client.Client;
import core.client.ClientCreator;
import core.client.ClientInvokeResponse;
import core.db.DB;
import core.db.Zone;
import core.nemesis.Nemesis;
import core.nemesis.NemesisGenerator;
import core.nemesis.NemesisGenerators;
import core.nemesis.NemesisOperation;
import core.record.ActionEnum;
import core.record.Operation;
import core.record.Recorder;
import lombok.extern.slf4j.Slf4j;
import util.Constant;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Slf4j
public class Controller {

    private ControlConfig config;
    private ArrayList<Client> clients;
    private NemesisGenerators generators;
    private ReentrantReadWriteLock lock;
    private Recorder recorder;
//    private Model model;

    public Controller(ControlConfig config, ClientCreator clientCreator, String nemesisNames, Recorder recorder) {
        this.config = config;
        clients = new ArrayList<>();
        for(Zone zone: config.getZones())
            clients.add(clientCreator.Create(zone));
        this.generators = NemesisGenerators.ParseNemesisGenerators(nemesisNames);
        this.lock = new ReentrantReadWriteLock();
        this.recorder = recorder;
    }

    public void Run() {

        SetUpDB();
        SetUpClient();

        int threadNum = this.config.getClientCount();
        CountDownLatch cdl = new CountDownLatch(threadNum);
        for(int i = 0; i < threadNum; i++) {
            Client client = this.clients.get(i);
            new Thread(() -> {
                this.InvokeClientWithRecord(client);
                cdl.countDown();
            }).start();
        }

        Thread nemesisThread = new Thread(this::DispatchNemesis);
        nemesisThread.start();

        try {
            cdl.await();
            nemesisThread.join();
        } catch (Exception e) {
            log.error(e.getMessage());
        }



        TearDownClient();
        // TODO TearDownDB()?
    }

    private void SetUpDB() {
        DB db = Constant.GetDB(this.config.getDBName());
        if(db == null)
            return;
        for(Zone zone: this.config.getZones()) {
            Exception exception = db.SetUp(zone);
            if(exception != null)
                log.error(exception.getMessage());
        }
    }

    private void SetUpClient() {
        ArrayList<Zone> zones = this.config.getZones();
        for(int i = 0; i < zones.size(); i++) {
            Zone zone = zones.get(i);
            try {
                Connection connection = DriverManager.getConnection(zone.getOceanBaseURL(), zone.getUsername(), zone.getPassword());
                this.clients.get(i).setConnection(connection);
                log.info("Set up client in " + zone.getIp());
                for(Client client: this.clients)
                    client.SetUp();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void TearDownClient() {
        for(Client client: this.clients) {
            try {
                client.TearDown();
                client.getConnection().close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void DispatchNemesis() {
        while (true) {
            this.lock.readLock().lock();
            NemesisGenerators generators = this.generators;
            this.lock.readLock().unlock();

            if(generators.HasNext()) {
                NemesisGenerator nemesisGenerator = generators.Next();
                ArrayList<NemesisOperation> operations = nemesisGenerator.Generate(this.config.getZones());
                CountDownLatch cdl = new CountDownLatch(operations.size());

                for(NemesisOperation operation: operations) {
                    new Thread(() -> {
                        OnNemesis(operation);
                        cdl.countDown();
                    }).start();
                }
                try {
                    cdl.await();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
            else {
                generators.Reset();
                break;
            }
        }
    }

    private void OnNemesis(NemesisOperation nemesisOperation) {
        String ip = nemesisOperation.getZone().getIp();
        Nemesis nemesis = Constant.GetNemesis(nemesisOperation.getNemesisName());
        if(nemesis == null) {
            log.warn("Nemesis " + nemesisOperation.getNemesisName() + " hasn't been registered!");
            return;
        }

        log.info("Nemesis " + nemesis.Name() + " is running to " + ip + "...");
//        this.recorder.RecordHistory();      // TODO Nemesis Invoke Operation
        Exception exception = nemesis.Invoke(nemesisOperation.getZone(), nemesisOperation.getInvokeArgs());
        if(exception != null)
            log.error("Run nemesis " + nemesis.Name() + " failed: " + exception.getMessage());


        try {
            log.info("Continuous nemesis in " + ip + " for " + nemesisOperation.getRunTime().toMillis() + " milliseconds...");
            Thread.sleep(nemesisOperation.getRunTime().toMillis());    // 直接把time.duration的值变成毫秒级给sleep()
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        log.info("Nemesis " + nemesis.Name() + " in "+ ip +" is recovering...");
//        this.recorder.RecordHistory();      // TODO Nemesis Recover Operation
        exception = nemesis.Recover(nemesisOperation.getZone(), nemesisOperation.getRecoverArgs());        // TODO maybe retry it many times in a specific interval
        if(exception != null)
            log.error("Recover nemesis " + nemesis.Name() + " failed: " + exception.getMessage());
    }

    private void InvokeClientWithRecord(Client client) {
        int threadId = (int) Thread.currentThread().getId();
        for(int i = 0; i < client.getRequestCount(); i++) {
            Object request = client.NextRequest();
            Operation<?> operation = new Operation<>(i, threadId, ActionEnum.InvokeOperation, new Date(), request);
            this.recorder.RecordHistory(operation);
            log.info(operation.toString());

            ClientInvokeResponse<?> response = client.Invoke(request);
            operation = new Operation<>(i, threadId, ActionEnum.ResponseOperation, new Date(), response);
            this.recorder.RecordHistory(operation);
            log.info(operation.toString());
        }
    }
}