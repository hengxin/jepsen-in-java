package core.control;

import core.client.Client;
import core.client.ClientCreator;
import core.db.DB;
import core.db.Zone;
import core.nemesis.Nemesis;
import core.nemesis.NemesisGenerator;
import core.nemesis.NemesisGenerators;
import core.nemesis.NemesisOperation;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Controller {

    private ControlConfig config;
    private ArrayList<Client> clients;
    private NemesisGenerators generators;
    private ReentrantReadWriteLock lock;

    public Controller(ControlConfig config, ClientCreator clientCreator, String nemesisNames) {
        this.config = config;
        for(Zone zone: config.getZones())
            clients.add(clientCreator.Create(zone));
        this.generators = NemesisGenerators.ParseNemesisGenerators(nemesisNames);
        this.lock = new ReentrantReadWriteLock();
    }

    public void SendControlToClient() {

        SetUpDB();

        Thread nemesisThread = new Thread(() -> DispatchNemesis());
        nemesisThread.start();

        int threadNum = Math.min(this.clients.size(), this.config.getClientCount());
        CountDownLatch cdl = new CountDownLatch(threadNum);
        for(int i = 0; i < threadNum; i++) {
            Client client = this.clients.get(i);
            new Thread(() -> {
                client.Start();     // TODO parameters
                cdl.countDown();
            }).start();
        }

        try {
            cdl.await();
            nemesisThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SetUpDB() {
        DB db = DB.GetDB(this.config.getDBName());
        if(db == null)
            return;
        for(Zone zone: this.config.getZones()) {
            Exception exception = db.SetUp(zone);
            if(exception != null)
                System.out.println(exception.getMessage());
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
                    e.printStackTrace();
                }
            }
            else {
                generators.Reset();
                break;
            }
        }
    }

    private void OnNemesis(NemesisOperation nemesisOperation) {
        Nemesis nemesis = Nemesis.GetNemesis(nemesisOperation.getNemesisName());
        if(nemesis == null) {
            System.out.println("Nemesis " + nemesis.Name() + " hasn't been registered!");
            return;
        }

        System.out.println("Nemesis " + nemesis.Name() + " is running...");
        Exception exception = nemesis.Invoke(nemesisOperation.getZone());
        if(exception != null) {
            System.out.println("Run nemesis " + nemesis.Name() + " failed: " + exception.getMessage());
        }

        System.out.println("Nemesis " + nemesis.Name() + " is recovering...");
        exception = nemesis.Recover(nemesisOperation.getZone());        // TODO maybe retry it many times in a specific interval
        if(exception != null) {
            System.out.println("Recover nemesis " + nemesis.Name() + " failed: " + exception.getMessage());
        }
    }
}