package core.checker;

import java.util.Map;

public class Op {
    public static boolean isOk(Map op) {
        return op.get("type").equals("ok");
    }

    public static boolean isFail(Map op) {
        return op.get("type").equals("fail");
    }

    public static boolean isInfo(Map op) {
        return op.get("type").equals("info");
    }

    public static boolean isInvoke(Map op) {
        return op.get("type").equals("invoke");
    }
}
