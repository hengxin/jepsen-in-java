package core.checker;

import util.ClojureCaller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A counter starts at zero; add operations should increment it by that much,
 *   and reads should return the present value. This checker validates that at
 *   each read, the value is greater than the sum of all :ok increments, and lower
 *   than the sum of all attempted increments.
 *
 *   Note that this counter verifier assumes the value monotonically increases:
 *   decrements are not allowed.
 */
public class Counter implements Checker {
    @Override
    public Map check(Map test, List<Map> history, Map opts) {
        history = (List<Map>) ClojureCaller.call("knossos.history", "complete", history);
        history.removeIf(h -> (boolean) h.get("fails?"));
        history.removeIf(Op::isFail);

        long lower = 0, upper = 0;
        Map pendingReads = new HashMap<>();
        List<Long> reads = new ArrayList();
        while (!history.isEmpty()) {

            Map op = history.get(0);
            history = history.subList(1, history.size());
            Object type = op.get("type"), f = op.get("f");
            if (type.equals("invoke") && f.equals("read")) {
                pendingReads.put(op.get("process"), List.of(lower, op.get("value")));
            } else if (type.equals("ok") && f.equals("read")) {
                List r = (List) pendingReads.get(op.get("process"));
                pendingReads.remove(op.get("process"));
                reads.addAll(r);
                reads.add(upper);
            } else if (type.equals("invoke") && f.equals("add")) {
                assert (long) op.get("value") >= 0;
                upper = upper + (long) op.get("value");
            } else if (type.equals("ok") && f.equals("add")) {
                lower = lower + (long) op.get("value");
            }


        }
        List<Long> errors = new ArrayList<>(reads);
        long finalUpper = upper;
        long finalLower = lower;
        errors.removeIf(e -> e > finalUpper || e < finalLower); //TODO check
        return Map.of(
                "valid?", errors.isEmpty(),
                "reads", reads,
                "error", errors
        );
    }
}
