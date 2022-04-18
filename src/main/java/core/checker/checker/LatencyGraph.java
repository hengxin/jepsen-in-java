package core.checker.checker;

import core.checker.util.Perf;
import core.checker.vo.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spits out graphs of latencies. Checker options take precedence over
 * those passed in with this constructor.
 */
public class LatencyGraph implements Checker {
    Map<String, ?> opts;

    public LatencyGraph(Map opts) {
        this.opts = opts;
    }

    public LatencyGraph() {
        this.opts = new HashMap<>();
    }

    @Override
    public Result check(Map test, List<Operation> history, Map cOpts) {
        Map<String, Object> o = new HashMap<>();
        for (String key : opts.keySet()) {
            o.put(key, opts.get(key));
        }

        for (Object key : cOpts.keySet()) {
            o.put(key.toString(), cOpts.get(key));
        }
        Perf.pointGraph(test, history, o);
        Perf.quantilesGraph(test, history, o);
        Result result = new Result();
        result.setValid(true);
        return result;
    }
}
