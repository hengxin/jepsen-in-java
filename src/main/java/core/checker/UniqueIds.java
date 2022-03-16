package core.checker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Checks that a unique id generator actually emits unique IDs. Expects a
 *   history with :f :generate invocations matched by :ok responses with distinct
 *   IDs for their :values. IDs should be comparable
 */
public class UniqueIds implements Checker {

    @Override
    public Map check(Map test, List<Map> history, Map opts) {
        long attemptCount = history.stream()
                .filter(Op::isInvoke)
                .filter(op -> op.get("f").equals("generate"))
                .count();

        List<Long> acks = history.stream()
                .filter(Op::isOk)
                .filter(op -> op.get("f").equals("generate"))
                .map(op -> (long) op.get("value")).collect(Collectors.toList());

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

        //TODO sort dups by value
        return Map.of(
                "valid?", dups.isEmpty(),
                "attempted-count", attemptCount,
                "acknowledged-count", acks.size(),
                "duplicated-count", dups.size(),
                "duplicated", dups,
                "range", range
        );

    }
}
