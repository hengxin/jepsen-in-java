package core.checker;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Computes basic statistics about success and failure rates, both overall and
 *   broken down by :f. Results are valid only if every :f has at some :ok
 *   operations; otherwise they're :unknown.
 */
public class Stats implements Checker {
    @Override
    public Map check(Map test, List<Map> history, Map opts) {
        history.removeIf(Op::isInvoke);
        history.removeIf(h -> h.get("process").equals("nemesis"));    //TODO confirm
        Map f_h = history.stream().collect(Collectors.groupingBy(h -> h.get("f")));
        for (Object f : f_h.keySet()) {
            List subhistory = (List) f_h.get(f);
            f_h.put(f, CheckerUtil.statsHelper(subhistory));
        }
        Map<Object, Map> groups = new TreeMap<Object, Map>(f_h);
        Map res = CheckerUtil.statsHelper(history);
        res.put("by-f", groups);
        res.put("valid?", CheckerUtil.mergeValid(groups.values().stream().map(g -> g.get("valid?")).collect(Collectors.toList())));
        return res;
    }
}
