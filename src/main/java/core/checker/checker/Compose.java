package core.checker.checker;

import core.checker.vo.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Takes a map of names to checkers, and returns a checker which runs each
 * check (possibly in parallel) and returns a map of names to results; plus a
 * top-level :valid? key which is true iff every checker considered the history
 * valid.
 */
public class Compose implements Checker {
    Map<String, Checker> checkerMap;
    CheckerUtil checkerUtil = new CheckerUtil();

    public Compose(Map<String, Checker> checkerMap) {
        this.checkerMap = checkerMap;
    }


    /**
     * TODO parallel run
     */
    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        Map<String, Result> results = new HashMap<>();
        Map<String, Object> res = new HashMap<>();
        for (Map.Entry<String, Checker> entry : checkerMap.entrySet()) {
            String k = entry.getKey();
            Checker checker = entry.getValue();
            results.put(k, checkerUtil.checkSafe(checker, test, history, opts));
        }
        List<Object> valids = results.values().stream().map(Result::getValid).collect(Collectors.toList());

        Result result = new Result();
        Object valid = CheckerUtil.mergeValid(valids);
        res.put("valid?", valid);
        result.setResults(results);
        result.setRes(res);
        result.setValid(valid);
        return result;
    }
}
