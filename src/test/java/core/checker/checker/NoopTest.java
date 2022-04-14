package core.checker.checker;

import core.checker.checker.Noop;
import core.checker.checker.Operation;
import core.checker.checker.TotalQueue;
import core.checker.vo.Result;
import jdk.jshell.execution.FailOverExecutionControlProvider;
import knossos.op.Op;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;

class NoopTest {
    Noop noop = new Noop();

    @Test
    void check() {
        Map map = Map.of();
        Operation operation = new Operation();
        assertNull(noop.check(map, List.of(operation), map));
    }


}