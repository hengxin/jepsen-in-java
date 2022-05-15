package core.control;

import core.checker.checker.Linearizable;
import core.checker.checker.Operation;
import core.checker.model.Model;
import core.checker.vo.Result;
import core.client.Client;
import core.client.ClientCreator;
import core.client.ClientInvokeResponse;
import core.client.ClientRequest;
import core.db.DB;
import core.db.Node;
import core.nemesis.Nemesis;
import core.nemesis.NemesisGenerator;
import core.nemesis.NemesisGenerators;
import core.nemesis.NemesisOperation;
import core.record.Recorder;
import lombok.extern.slf4j.Slf4j;
import util.Constant;
import util.Support;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static core.checker.checker.Operation.Type.*;


@Slf4j
public class Controller {

    private ControlConfig config;
    private ArrayList<Client> clients;
    private NemesisGenerators generators;
    private ReentrantReadWriteLock lock;
    private Recorder recorder;
    private Linearizable linearizable;

    public Controller(ControlConfig config, ClientCreator clientCreator, String nemesisNames, Recorder recorder,
                      String checkAlgorithm, Model model) {
        this.config = config;
        clients = new ArrayList<>();
        for(Node node : config.getNodes())
            clients.add(clientCreator.Create(node));
        this.generators = NemesisGenerators.ParseNemesisGenerators(nemesisNames);
        this.lock = new ReentrantReadWriteLock();
        this.recorder = recorder;
        this.linearizable = new Linearizable(new HashMap(Map.of("algorithm", checkAlgorithm, "model", model)));
    }

    public void Run() {
        new Constant().Init();

        // TODO 这里的异常选择怎样处理
//        SetUpDB();        // TODO 删掉注释
        SetUpClient();          // TODO 这里有异常必须终止 后面都跑不了


        LocalDateTime startTime = LocalDateTime.now();
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

        // TODO 感觉这里很多用map的应该抽象成类的
        ArrayList<Operation> operations = Support.TxtToOperations(this.recorder.GetRecordFilePath());
        Result result = this.linearizable.check(new HashMap(Map.of("name", this.config.getDbName(), "start-time", startTime)), operations, new HashMap<>());
        if((boolean)result.getValid())
            log.info("Congratulations! The whole process has passed the linearizable check.");
        else
            log.info("Sorry, the process is not linearizable! Please view the detail in store directory.");
    }

    private void SetUpDB() {
        DB db = Constant.GetDB(this.config.getDbName());
        if(db == null)
            return;
        for(Node node : this.config.getNodes()) {
            Exception exception = db.SetUp(node);
            if(exception != null)
                log.error(exception.getMessage());
        }
        db.SetConfig(this.config.getNodes());
    }

    private void SetUpClient() {
        ArrayList<Node> nodes = this.config.getNodes();
        for(int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            try {
                log.info("Set up client in " + node.getIp());
                this.clients.get(i).SetUp(node);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void TearDownClient() {
        for(Client client: this.clients) {
            try {
                client.TearDown();
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
                ArrayList<NemesisOperation> operations = nemesisGenerator.Generate(this.config.getNodes());
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
        String ip = nemesisOperation.getNode().getIp();
        Nemesis nemesis = Constant.GetNemesis(nemesisOperation.getNemesisName());
        if(nemesis == null) {
            log.warn("Nemesis " + nemesisOperation.getNemesisName() + " hasn't been registered!");
            return;
        }

        log.info("Nemesis " + nemesis.Name() + " is running to " + ip + "...");
//        this.recorder.RecordHistory();      // TODO Nemesis Invoke Operation
        Exception exception = nemesis.Invoke(nemesisOperation.getNode(), nemesisOperation.getInvokeArgs());
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
        exception = nemesis.Recover(nemesisOperation.getNode(), nemesisOperation.getRecoverArgs());        // TODO maybe retry it many times in a specific interval
        if(exception != null)
            log.error("Recover nemesis " + nemesis.Name() + " failed: " + exception.getMessage());
    }

    private void InvokeClientWithRecord(Client client) {
        int threadId = (int) Thread.currentThread().getId();
        for(int i = 0; i < client.getRequestCount(); i++) {
            ClientRequest request = client.NextRequest();
            Operation operation = new Operation(Integer.parseInt("" + i + threadId), INVOKE, request.getValue(), request.getFunction(), System.currentTimeMillis());
            this.recorder.RecordHistory(operation);
            log.info(operation.toString());

            ClientInvokeResponse<?> response = client.Invoke(request);
            if(response.isSuccess())
                operation = new Operation(Integer.parseInt("" + i + threadId), OK, response.getNewState(), request.getFunction(), System.currentTimeMillis());
            else
                operation = new Operation(Integer.parseInt("" + i + threadId), FAIL, response.getNewState(), request.getFunction(), System.currentTimeMillis());
            this.recorder.RecordHistory(operation);
            log.info(operation.toString());
        }
    }
}