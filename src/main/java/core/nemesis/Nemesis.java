package core.nemesis;

import core.db.Zone;

import java.util.Map;

public interface Nemesis {
    // Not static methods
    Exception Invoke(Zone zone, Map<String, String> invokeArgs);

    Exception Recover(Zone zone, Map<String, String> recoverArgs);

    String Name();
}
