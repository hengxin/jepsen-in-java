package core.checker;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    public Map check(Map test, List<Map> history, Map opts) {
        Map<String, Object> results = new TreeMap<>(Comparator.reverseOrder());
        for (Map.Entry<String, Checker> entry : checkerMap.entrySet()) {
            String k = entry.getKey();
            Checker checker = entry.getValue();
            results.put(k, checkerUtil.checkSafe(checker, test, history, opts));
        }
        List valids = results.values().stream().map(v -> ((Map) v).get("valid?")).collect(Collectors.toList());
        results.put("valid?", checkerUtil.mergeValid(valids));
        return results;
    }
}
