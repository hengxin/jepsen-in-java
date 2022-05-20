package core.checker.checker;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HtmlTest {

    @Test
    void check() {
        Html html = new Html();
        Operation op1 = Operation.invoke(0, Operation.F.READ, null);
        Operation op2 = Operation.ok(0, Operation.F.READ, 0);
        List<Operation> history = new ArrayList<>(List.of(op1, op2));
        html.check(new HashMap<>(Map.of(
                "name", "html test",
                "start-time", LocalDateTime.now(),
        "time", LocalDateTime.now()
        )),history, new HashMap<>());
    }
}