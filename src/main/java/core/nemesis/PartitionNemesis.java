package core.nemesis;

import core.db.Zone;

import java.util.ArrayList;

public class PartitionNemesis implements Nemesis {
    @Override
    public Exception Invoke(Zone zone) {
        return null;
    }

    @Override
    public Exception Recover(Zone zone) {
        return null;
    }

    @Override
    public String Name() {
        return PARTITION_NODE;
    }
}

class PartitionGenerator implements NemesisGenerator {
    @Override
    public ArrayList<NemesisOperation> Generate(ArrayList<Zone> zones) {
        return null;
    }
}
