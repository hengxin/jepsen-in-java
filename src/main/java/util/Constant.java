package util;

import core.db.DB;
import core.db.OceanbaseDB;
import core.nemesis.KillNemesis;
import core.nemesis.Nemesis;
import core.nemesis.PartitionNemesis;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class Constant {

    public static final int SSH_PORT = 22;

    // All nemesis names
    public static final String NEMESIS_KILL_NODE = "kill";
    public static final String NEMESIS_PARTITION_NODE = "partition_network";

    // All nemesis generator names
    public static final String NEMESIS_GENERATOR_RANDOM_KILL = "random_kill";
    public static final String NEMESIS_GENERATOR_ALL_KILL = "all_kill";
    public static final String NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION = "symmetric_network_partition";
    public static final String NEMESIS_GENERATOR_ASYMMETRIC_NETWORK_PARTITION = "asymmetric_network_partition";


    public static HashMap<String, DB> DB_MAP = new HashMap<>();
    public static HashMap<String, Nemesis> NEMESIS_MAP = new HashMap<>();


    public void Init() {
        // Register DB
        OceanbaseDB oceanbaseDB = new OceanbaseDB();
        this.RegisterDB(oceanbaseDB);


        // Register Nemesis
        KillNemesis killNemesis = new KillNemesis();
        this.RegisterNemesis(killNemesis);
        PartitionNemesis partitionNemesis = new PartitionNemesis();
        this.RegisterNemesis(partitionNemesis);
    }

    public void RegisterDB(DB db) {
        if(!DB_MAP.containsKey(db.Name()))
            DB_MAP.put(db.Name(), db);
        else
            log.warn("Duplicate db key " + db.Name() + ", discarded!");
    }

    public void RegisterNemesis(Nemesis nemesis) {
        if(!NEMESIS_MAP.containsKey(nemesis.Name()))
            NEMESIS_MAP.put(nemesis.Name(), nemesis);
        else
            log.warn("Duplicate nemesis key " + nemesis.Name() + ", discarded!");
    }

    public static DB GetDB(String name) {
        return DB_MAP.get(name);        // attention maybe null
    }

    public static Nemesis GetNemesis(String name) {
        return NEMESIS_MAP.get(name);           // attention maybe null
    }

}
