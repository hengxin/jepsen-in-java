package core.nemesis;

import core.db.Node;

import java.util.Map;

public interface Nemesis {
    Exception Invoke(Node node, Map<String, String> invokeArgs);

    Exception Recover(Node node, Map<String, String> recoverArgs);

    String Name();
}
