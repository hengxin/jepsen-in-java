package core.checker.checker;

import core.checker.util.OpUtil;
import core.checker.vo.Result;
import util.Util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Given a set of :add operations followed by a final :read, verifies that
 *   every successfully added element is present in the read, and that the read
 *   contains only elements for which an add was attempted
 */
public class SetChecker implements Checker {

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        Set<Integer> attempts = history.stream()
                .filter(OpUtil::isInvoke)
                .filter(h -> h.getF() == Operation.F.ADD)
                .map(h -> (int) h.getValue())
                .collect(Collectors.toSet());

        Set<Integer> adds = history.stream()
                .filter(OpUtil::isOk)
                .filter(h -> h.getF() == Operation.F.ADD)
                .map(h -> (int) h.getValue())
                .collect(Collectors.toSet());

        List<Integer> finalRead = history.stream()
                .filter(OpUtil::isOk)
                .filter(h -> h.getF() == Operation.F.READ)
                .map(h -> (int) h.getValue())
                .collect(Collectors.toList());   //TODO check reduce

        if (finalRead.size() == 0) {
            Result result = new Result();
            result.setValid("unknown");
            result.setError("Set was never read");
            return result;
        } else {
            Set<Integer> finalReadSet = new HashSet<>(finalRead);
            Set<Integer> ok = new HashSet<>(finalReadSet);
            ok.retainAll(attempts);
            Set<Integer> unexpected = new HashSet<>(finalReadSet);
            unexpected.removeAll(attempts);
            Set<Integer> lost = new HashSet<>(adds);
            lost.removeAll(finalReadSet);
            Set<Integer> recovered = new HashSet<>(ok);
            recovered.removeAll(adds);
            Map<String, Object> res = new HashMap<>(Map.of(
                    "valid?", lost.isEmpty() && unexpected.isEmpty(),
                    "attempt-count", attempts.size(),
                    "acknowledged-count", adds.size(),
                    "ok-count", ok.size(),
                    "lost-count", lost.size(),
                    "recovered-count", recovered.size(),
                    "unexpected-count", unexpected.size(),
                    "ok", Util.integerIntervalSetStr(ok),
                    "lost", Util.integerIntervalSetStr(lost),
                    "unexpected", Util.integerIntervalSetStr(unexpected)
            ));
            res.put("recovered", Util.integerIntervalSetStr(recovered));
            Result result = new Result();
            result.setValid(lost.isEmpty() && unexpected.isEmpty());
            result.setRes(res);
            return result;
        }

    }
}
