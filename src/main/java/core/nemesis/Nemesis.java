package core.nemesis;

import core.db.Zone;

import java.util.HashMap;

public interface Nemesis {
     // All nemesis names
    String KILL_NODE = "kill";

    // Not static methods
    Exception Invoke(Zone zone);

    Exception Recover(Zone zone);

    String Name();
}
