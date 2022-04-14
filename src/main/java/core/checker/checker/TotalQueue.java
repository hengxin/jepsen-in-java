package core.checker.checker;

import com.google.common.collect.HashMultiset;
import core.checker.util.OpUtil;
import core.checker.vo.Result;

import java.util.*;
import java.util.stream.Collectors;

/**
 * What goes in *must* come out. Verifies that every successful enqueue has a
 *   successful dequeue. Queues only obey this property if the history includes
 *   draining them completely. O(n).
 */
public class TotalQueue implements Checker {
    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        history = CheckerUtil.expandQueueDrainOps(history);
        HashMultiset<Object> attempts = HashMultiset.create();
        attempts.addAll(history.stream()
                .filter(OpUtil::isInvoke)
                .filter(op -> op.getF() == Operation.F.ENQUEUE)
                .map(Operation::getValue).collect(Collectors.toList()));

        HashMultiset<Object> enqueues = HashMultiset.create();
        enqueues.addAll(history.stream()
                .filter(OpUtil::isOk)
                .filter(op -> op.getF() == Operation.F.ENQUEUE)
                .map(Operation::getValue).collect(Collectors.toList()));

        HashMultiset<Object> dequeues = HashMultiset.create();
        dequeues.addAll(history.stream()
                .filter(OpUtil::isOk)
                .filter(op -> op.getF() == Operation.F.DEQUEUE)
                .map(Operation::getValue).collect(Collectors.toList()));
        HashMultiset<Object> ok = HashMultiset.create(dequeues);
        intersect(ok, attempts);

        HashMultiset<Object> unexpected = HashMultiset.create(dequeues);
        unexpected.removeAll(attempts);


        HashMultiset<Object> duplicated = HashMultiset.create(dequeues);
        minus(duplicated, attempts);
        minus(duplicated, unexpected);

        HashMultiset<Object> lost = HashMultiset.create(enqueues);
        minus(lost, dequeues);

        HashMultiset<Object> recovered = HashMultiset.create(ok);
        minus(recovered, enqueues);

        Map<String, Object> res = new HashMap<>(Map.of(
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

        ));
        res.put("duplicated", duplicated);
        res.put("recovered", recovered);
        Result result = new Result();
        result.setValid(lost.isEmpty() && unexpected.isEmpty());
        result.setRes(res);
        return result;
    }

    private void intersect(HashMultiset<Object> a, HashMultiset<Object> b) {
        Set<Object> elements = new HashSet<>(a.elementSet());
        for (Object o : elements) {
            if (!b.contains(o)) {
                a.remove(o, a.count(o));
            } else {
                a.remove(o, Math.max(a.count(o) - b.count(o), 0));
            }
        }
    }

    private void minus(HashMultiset<Object> a, HashMultiset<Object> b) {
        Set<Object> elements = new HashSet<>(a.elementSet());
        for (Object o : elements) {
            if (b.contains(o)) {
                a.remove(o, b.count(o));
            }
        }
    }
}
