package core.checker;

import util.ClojureCaller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spits out graphs of throughput over time. Checker options take precedence
 *   over those passed in with this constructor.
 */
public class RateGraph implements Checker {
    private Map opts;

    public RateGraph(Map opts) {
        this.opts = opts;
    }

    @Override
    public Map check(Map test, List<Map> history, Map cOpts) {
        Map o = new HashMap();
        for (Object key : opts.keySet()) {
            o.put(key, opts.get(key));
        }
        for (Object key : cOpts.keySet()) {
            o.put(key, cOpts.get(key));
        }
        ClojureCaller.call("jepsen.checker.perf", "rate-graph!", test, history, o);
        return Map.of(
                "valid?", true
        );
    }
}
