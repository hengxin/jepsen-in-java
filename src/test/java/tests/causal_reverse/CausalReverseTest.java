package tests.causal_reverse;

import core.checker.checker.Checker;
import core.checker.checker.Linearizable;
import core.checker.checker.Operation;
import org.junit.jupiter.api.Test;
import tests.Invoke;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CausalReverseTest {

    @Test
    void sequentialHistories() {
        Checker checker = new CausalReverseChecker();
        List<Operation> valid = new ArrayList<>(List.of(
                new Operation(0, Operation.Type.INVOKE, Operation.F.WRITE, 1)

        ));
    }

}