package core.checker.checker;

import core.checker.checker.Operation;
import core.checker.checker.Stats;
import core.checker.vo.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class StatsTest {

    @Test
    void check() {
        Operation operation1 = new Operation(Operation.F.FOO, Operation.Type.OK);
        Operation operation2 = new Operation(Operation.F.FOO, Operation.Type.FAIL);
        Operation operation3 = new Operation(Operation.F.BAR, Operation.Type.INFO);
        Operation operation4 = new Operation(Operation.F.BAR, Operation.Type.FAIL);
        Operation operation5 = new Operation(Operation.F.BAR, Operation.Type.FAIL);
        List<Operation> history = new ArrayList<>(List.of(operation1, operation2, operation3, operation4, operation5));
        Stats stats = new Stats();
        Result result = stats.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), false);
    }
}