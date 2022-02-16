package core.control;

import core.client.Client;
import core.nemesis.Nemesis;
import core.nemesis.NemesisGenerators;
import core.nemesis.NemesisOperation;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Controller {

    private ControlConfig controlConfig;
    private ArrayList<Client> clients;
    private NemesisGenerators generators;
    private ReentrantReadWriteLock lock;

    public Controller(ControlConfig controlConfig, ArrayList<Client> clients, String nemesisNames) {
        this.controlConfig = controlConfig;
        this.clients = clients;     // TODO make it easier [ClientCreator]
        this.generators = NemesisGenerators.ParseNemesisGenerators(nemesisNames);
        this.lock = new ReentrantReadWriteLock();
    }

    private void DispatchNemesis() {
        lock.readLock().lock();
        NemesisGenerators generators = this.generators;
        lock.readLock().unlock();
        while (generators.HasNext()) {
            ArrayList<NemesisOperation> operations = generators.Next().Generate(this.controlConfig.getZones());
            for(NemesisOperation operation: operations) {

            }
        }
        generators.Reset();
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
    }
}