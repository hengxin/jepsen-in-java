package core.checker.checker;

import org.junit.jupiter.api.Test;

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