package core.checker.checker;

import core.checker.linearizability.History;
import core.checker.util.OpUtil;
import core.checker.vo.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A counter starts at zero; add operations should increment it by that much,
 * and reads should return the present value. This checker validates that at
 * each read, the value is greater than the sum of all :ok increments, and lower
 * than the sum of all attempted increments.
 * <p>
 * Note that this counter verifier assumes the value monotonically increases:
 * decrements are not allowed.
 */
public class Counter implements Checker {
    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        history = History.complete(history);
        history.removeIf(Operation::isFail);
        history.removeIf(OpUtil::isFail);

        long lower = 0, upper = 0;
        Map<Object, List<Long>> pendingReads = new HashMap<>();
        List<List<Long>> reads = new ArrayList<>();
        while (!history.isEmpty()) {
            Operation operation = history.get(0);
            history = history.subList(1, history.size());
            Operation.Type type = operation.getType();
            Operation.F f = operation.getF();
            if (type == Operation.Type.INVOKE && f == Operation.F.READ) {
                pendingReads.put(operation.getProcess(), new ArrayList<>(List.of(lower, Long.parseLong(operation.getValue().toString()))));
            } else if (type == Operation.Type.OK && f == Operation.F.READ) {
                List<Long> r = pendingReads.get(operation.getProcess());
                pendingReads.remove(operation.getProcess());
                r.add(upper);
                reads.add(r);
            } else if (type == Operation.Type.INVOKE && f == Operation.F.ADD) {
                assert (int) operation.getValue() >= 0;
                upper = upper + (int) operation.getValue();
            } else if (type == Operation.Type.OK && f == Operation.F.ADD) {
                lower = lower + (int) operation.getValue();
            }


        }
        List<List<Long>> errors = new ArrayList<>(reads);
        errors.removeIf(e -> {
            if (e.isEmpty()) return true;
            long prev = e.get(0);
            for (long value : e) {
                if (value < prev) return false;
                prev = value;
            }
            return true;
        });
        Result result = new Result();
        result.setValid(errors.isEmpty());
        result.setReads(reads);
        result.setError(errors);
        Map<String, Object> res = new HashMap<>(Map.of(
                "valid?", errors.isEmpty(),
                "reads", reads,
                "errors", errors

        ));
        result.setRes(res);
        return result;
    }
}
