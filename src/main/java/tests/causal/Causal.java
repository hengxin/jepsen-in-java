package tests.causal;

import core.checker.checker.Operation;
import tests.Invoke;

import java.util.HashMap;
import java.util.Map;

public class Causal {

    public static CausalRegister causalRegister() {
        return new CausalRegister(0, 0, null);
    }


    public static Invoke r() {
        return new Invoke(Operation.Type.INVOKE, Operation.F.READ, null);
    }

    public static Invoke ri() {
        return new Invoke(Operation.Type.INVOKE, Operation.F.READ_INIT, null);
    }

    public static Invoke cw1() {
        return new Invoke(Operation.Type.INVOKE, Operation.F.WRITE, 1);
    }

    public static Invoke cw2() {
        return new Invoke(Operation.Type.INVOKE, Operation.F.WRITE, 2);
    }

    public static Map<String, Object> test() {
        return new HashMap<>(Map.of(
                "checker", new CausalChecker(causalRegister())

        ));
    }


}
