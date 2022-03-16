package core.checker;

import util.Util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Given a set of :add operations followed by a final :read, verifies that
 *   every successfully added element is present in the read, and that the read
 *   contains only elements for which an add was attempted
 */
public class SetChecker implements Checker {

    @Override
    public Map check(Map test, List<Map> history, Map opts) {
        Set<Integer> attempts = history.stream()
                .filter(Op::isInvoke)
                .filter(h -> h.get("f").equals("add"))
                .map(h -> (int) h.get("value"))
                .collect(Collectors.toSet());

        Set<Integer> adds = history.stream()
                .filter(Op::isOk)
                .filter(h -> h.get("f").equals("add"))
                .map(h -> (int) h.get("value"))
                .collect(Collectors.toSet());

        List<Integer> finalRead = history.stream()
                .filter(Op::isOk)
                .filter(h -> h.get("f").equals("read"))
                .map(h -> (int) h.get("value"))
                .collect(Collectors.toList());   //TODO check reduce

        if (finalRead.size() == 0) {
            return Map.of(
                    "valid?", "unknown",
                    "error", "Set was never read"
            );
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
            Map res = Map.of(
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
            );
            res.put("recovered", Util.integerIntervalSetStr(recovered));
            return res;
        }

    }
}
