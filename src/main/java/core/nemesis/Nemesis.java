package core.nemesis;

import core.db.Zone;

import java.util.HashMap;

public interface Nemesis {
     // All nemesis names
    String KILL_NODE = "kill";

    HashMap<String, Nemesis> NEMESIS_MAP = new HashMap<>();

    static void RegisterNemesis(Nemesis nemesis) {
        if(!NEMESIS_MAP.containsKey(nemesis.Name()))
            NEMESIS_MAP.put(nemesis.Name(), nemesis);
        else
            System.out.println("Duplicate nemesis key " + nemesis.Name() + ", discarded!");
    }

    static Nemesis GetNemesis(String name) {
        return NEMESIS_MAP.get(name);           // attention maybe null
    }


    // Not static methods
    Exception Invoke(Zone zone);

    Exception Recover(Zone zone);

    String Name();
}
