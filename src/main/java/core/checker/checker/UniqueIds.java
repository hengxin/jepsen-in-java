package core.checker.checker;

import core.checker.util.OpUtil;
import core.checker.vo.Result;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Checks that a unique id generator actually emits unique IDs. Expects a
 *   history with :f :generate invocations matched by :ok responses with distinct
 *   IDs for their :values. IDs should be comparable
 */
public class UniqueIds implements Checker {

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        long attemptCount = history.stream()
                .filter(OpUtil::isInvoke)
                .filter(op -> op.getF().equals("generate"))
                .count();

        List<Long> acks = history.stream()
                .filter(OpUtil::isOk)
                .filter(op -> op.getF() == Operation.F.GENERATE)
                .map(op -> (long) op.getValue()).collect(Collectors.toList());

        Map<Object, Long> counts = new HashMap<>();
        for (long id : acks) {
            counts.put(id, counts.getOrDefault(id, 0L) + 1);
        }

        for (Object key : counts.keySet()) {
            if (counts.get(key) <= 1) {
                counts.remove(key);
            }
        }

        TreeMap dups = new TreeMap(counts);

        long lowest = acks.get(0);
        long highest = lowest;

        for (long id : acks) {
            if (id < lowest) {
                lowest = id;
            } else if (highest < id) {
                highest = id;
            }
        }

        List range = List.of(lowest, highest);

        Result result = new Result();
        result.setValid(dups.isEmpty());
        result.setRes(Map.of(
                "valid?", dups.isEmpty(),
                "attempted-count", attemptCount,
                "acknowledged-count", acks.size(),
                "duplicated-count", dups.size(),
                "duplicated", dups,
                "range", range
        ));

        //TODO sort dups by value
        return result;


    }
}
