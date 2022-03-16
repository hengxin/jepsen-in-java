package core.checker;

import util.ClojureCaller;

import java.util.List;
import java.util.Map;

/**
 * Plots clock offsets on all nodes
 */
public class ClockPlot implements Checker {
    @Override
    public Map check(Map test, List<Map> history, Map opts) {
        ClojureCaller.call("jepsen.checker.clock", "plot!", test, history, opts);
        return Map.of(
                "valid?", true
        );
    }
}
