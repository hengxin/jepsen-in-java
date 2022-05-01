package tests.linearizable_register;

import core.checker.checker.Checker;
import core.checker.checker.Compose;
import core.checker.checker.Linearizable;
import core.checker.checker.Operation;
import core.checker.model.CASRegister;
import core.checker.util.Html;
import tests.Invoke;

import java.util.*;

public class LinearizableRegister {
    static Random random = new Random();

    public static Operation w() {
        return new Invoke(Operation.F.WRITE, random.nextInt(5));

    }

    public static Operation r() {
        return new Invoke(Operation.F.READ, null);
    }

    public static Operation cas() {
        return new Invoke(Operation.F.CAS, new ArrayList<>(List.of(random.nextInt(5), random.nextInt(5))));
    }

    public static Map<String, Object> test(Map<String, Object> opts) {
        Map<String, Checker> compose = new HashMap<>(Map.of(
                "linearizable", new Linearizable(new HashMap<>(Map.of(
                        "model", opts.getOrDefault("model", new CASRegister(null))
                ))),

                "timeline", new Html()
        ));

        return new HashMap<>(Map.of(
                "checker", new Compose(compose)
        ));
    }
}
