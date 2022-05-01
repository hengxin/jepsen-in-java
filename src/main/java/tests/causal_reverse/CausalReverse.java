package tests.causal_reverse;


import core.checker.checker.Checker;
import core.checker.checker.CheckerUtil;
import core.checker.checker.Compose;
import core.checker.checker.Operation;
import core.checker.util.OpUtil;
import core.checker.vo.Result;
import tests.Invoke;
import tests.causal_reverse.CausalOp;

import java.util.*;
import java.util.stream.Collectors;

public class CausalReverse {
    public static Map<?, Set<Object>> graph(List<Operation> history) {
        Set<Object> completed = new TreeSet<>();
        Map<Object, Set<Object>> expected = new HashMap<>();
        for (Operation op : history) {
            if (op.getF() == Operation.F.WRITE) {
                if (OpUtil.isInvoke(op)) {
                    expected.put(op.getValue(), completed);
                    continue;
                }

                if (OpUtil.isOk(op)) {
                    completed.add(op.getValue());
                }

            }
        }
        return expected;
    }

    public static List<Operation> errors(List<Operation> history, Map<?, Set<Object>> expected) {
        List<Operation> h = history.stream().filter(OpUtil::isOk).filter(op -> op.getF() == Operation.F.READ).collect(Collectors.toList());
        List<Operation> errors = new ArrayList<>();
        for (Operation op : h) {
            List<?> seen = (List<?>) op.getValue();
            Set<Object> ourExpected = new HashSet<>();
            for (Object s : seen) {
                ourExpected.addAll(expected.get(s));
            }

            Set<Object> missing = new HashSet<>(ourExpected);
            seen.forEach(missing::remove);

            if (missing.isEmpty()) {
                continue;
            }
            CausalOp causalOp = new CausalOp(op);
            causalOp.setValue(null);
            causalOp.setMissing(missing);
            causalOp.setExpectedCount(ourExpected.size());
            errors.add(op);
        }
        return errors;
    }


    public static Operation r() {
        return new Invoke(Operation.Type.INVOKE, Operation.F.READ, null);
    }

    public static Operation w(Object k) {
        return new Invoke(Operation.F.WRITE, k);
    }

    public static Map<String, Object> workload() {
        Map<String, Checker> compose = new HashMap<>(Map.of(
                "perf", CheckerUtil.perf(),
                "sequential", new CausalReverseChecker()
        ));
        return new HashMap<>(Map.of(
                "checker", new Compose(compose)
        ));
    }
}
