package core.checker.util;

import core.checker.checker.Operation;
import core.checker.linearizability.Op;

import java.util.ArrayList;
import java.util.List;

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
