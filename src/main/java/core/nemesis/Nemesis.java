package core.nemesis;

import core.db.Zone;

public interface Nemesis {
     // All nemesis names
    String KILL_NODE = "kill";
    String PARTITION_NODE = "partition_network";

    // Not static methods
    Exception Invoke(Zone zone);

    Exception Recover(Zone zone);

    String Name();
}
