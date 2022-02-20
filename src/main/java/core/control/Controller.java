package core.control;

import core.client.Client;
import core.client.ClientCreator;
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
        Thread nemesisThread = new Thread(() -> DispatchNemesis());
        nemesisThread.start();

        CountDownLatch cdl = new CountDownLatch(this.config.getClientCount());
        for(int i = 0; i < this.config.getZones().size(); i++) {
            if(i >= this.config.getClientCount())
                break;
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
        exception = nemesis.Recover(nemesisOperation.getZone());        // TODO maybe try it repeatedly in a specific interval
        if(exception != null) {
            System.out.println("Recover nemesis " + nemesis.Name() + " failed: " + exception.getMessage());
        }
    }
}