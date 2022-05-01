package core.checker.util;

import core.checker.checker.Operation;

public class OpUtil {
    public static boolean isOk(Operation operation) {
        return operation.getType() == Operation.Type.OK;
    }

    public static boolean isFail(Operation operation) {
        return operation.getType() == Operation.Type.FAIL;
    }

    public static boolean isInfo(Operation operation) {
        return operation.getType() == Operation.Type.INFO;
    }

    public static boolean isInvoke(Operation operation) {
        return operation.getType() == Operation.Type.INVOKE;
    }


}
