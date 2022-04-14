package core.checker.checker;

import core.checker.vo.Result;
import util.ClojureCaller;

import java.util.List;
import java.util.Map;

/**
 * Plots clock offsets on all nodes
 */
public class ClockPlot implements Checker {
    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        ClojureCaller.call("jepsen.checker.clock", "plot!", test, history, opts);
        Result result = new Result();
        result.setValid(true);
        return result;
    }
}
