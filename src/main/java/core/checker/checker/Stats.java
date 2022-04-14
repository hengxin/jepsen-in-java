package core.checker.checker;

import core.checker.util.OpUtil;
import core.checker.vo.Result;

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
    public Result check(Map test, List<Operation> history, Map opts) {
        history.removeIf(OpUtil::isInvoke);
        history.removeIf(h -> h.getProcess() == -1);
        Map<Operation.F, List<Operation>> f_h = history.stream().collect(Collectors.groupingBy(Operation::getF));
        Map<Operation.F, Map<String, Object>> groups = new TreeMap<>();
        for (Operation.F f : f_h.keySet()) {
            List<Operation> subhistory = f_h.get(f);
            groups.put(f, CheckerUtil.statsHelper(subhistory));
        }
        Map<String, Object> res = CheckerUtil.statsHelper(history);
        res.put("by-f", groups);
        Object valid = CheckerUtil.mergeValid(groups.values().stream().map(g -> g.get("valid?")).collect(Collectors.toList()));
        res.put("valid?", valid);
        Result result = new Result();
        result.setValid(valid);
        result.setRes(res);
        return result;
    }
}
