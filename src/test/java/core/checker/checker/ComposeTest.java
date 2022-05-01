package core.checker.checker;

import core.checker.vo.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class ComposeTest {

    @Test
    void check() {
        Map<String,Checker> checkerMap=new HashMap<>(
                Map.of("a",new UnbridledOptimism(),
                        "b",new UnbridledOptimism())
        );
        Compose compose=new Compose(checkerMap);
        Result result=compose.check(null,null,new HashMap<>());
        Assertions.assertEquals(result.getValid(),true);
    }
}