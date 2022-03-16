package core.checker;

import com.google.common.collect.HashMultiset;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * What goes in *must* come out. Verifies that every successful enqueue has a
 *   successful dequeue. Queues only obey this property if the history includes
 *   draining them completely. O(n).
 */
public class TotalQueue implements Checker {
    @Override
    public Map check(Map test, List<Map> history, Map opts) {
        history = CheckerUtil.expandQueueDrainOps(history);
        HashMultiset attempts = HashMultiset.create();
        attempts.addAll(history.stream()
                .filter(Op::isInvoke)
                .filter(op -> op.get("f").equals("enqueue"))
                .map(op -> op.get("value")).collect(Collectors.toList()));

        HashMultiset enqueues = HashMultiset.create();
        enqueues.addAll(history.stream()
                .filter(Op::isOk)
                .filter(op -> op.get("f").equals("enqueue"))
                .map(op -> op.get("value")).collect(Collectors.toList()));

        HashMultiset dequeues = HashMultiset.create();
        dequeues.addAll(history.stream()
                .filter(Op::isOk)
                .filter(op -> op.get("f").equals("dequeue"))
                .map(op -> op.get("value")).collect(Collectors.toList()));
        HashMultiset ok = HashMultiset.create(dequeues);
        ok.retainAll(attempts);

        HashMultiset unexpected = HashMultiset.create(dequeues);
        Set keys = new HashSet(attempts);
        unexpected.removeAll(keys);

        HashMultiset duplicated = HashMultiset.create(unexpected);
        HashMultiset tmp = HashMultiset.create(attempts);
        tmp.removeAll(dequeues);
        duplicated.removeAll(tmp);

        HashMultiset lost = HashMultiset.create(enqueues);
        lost.removeAll(dequeues);

        HashMultiset recovered = HashMultiset.create(ok);
        recovered.removeAll(enqueues);

        Map res = Map.of(
                "valid?", lost.isEmpty() && unexpected.isEmpty(),
                "attempt-count", attempts.size(),
                "acknowledged-count", enqueues.size(),
                "ok-count", ok.size(),
                "unexpected-count", unexpected.size(),
                "duplicated-count", duplicated.size(),
                "lost-count", lost.size(),
                "recovered-count", recovered.size(),
                "lost", lost,
                "unexpected", unexpected

        );
        res.put("duplicated", duplicated);
        res.put("recovered", recovered);
        return res;
    }
}
