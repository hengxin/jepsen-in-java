package core.checker.checker;

import core.checker.vo.Result;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ClockPlotTest {

    @Test
    void check() {
        ClockPlot clockPlot = new ClockPlot();
        Operation op1 = new Operation(-1, 500000000, new HashMap<>(Map.of("n1", 2.1)));
        Operation op2 = new Operation(-1, 1000000000, new HashMap<>(Map.of("n1", 0d, "n2", -3.1)));
        Operation op3 = new Operation(-1, 1500000000, new HashMap<>(Map.of("n1", 1d, "n2", -2d)));
        Operation op4 = new Operation(-1, 2000000000, new HashMap<>(Map.of("n1", 2d, "n2", -4.1)));
        List<Operation> history = new ArrayList<>(List.of(op1, op2, op3, op4));
        Result result = clockPlot.check(new HashMap<>(Map.of(
                "name", "clock plot test",
                "start-time", 0
        )), history, new HashMap<>());
    }
}