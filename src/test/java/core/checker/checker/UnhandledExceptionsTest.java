package core.checker.checker;

import core.checker.checker.Operation;
import core.checker.checker.UnhandledExceptions;
import core.checker.vo.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.checker.checker.Operation.F.FOO;
import static core.checker.checker.Operation.F.valueOf;

class UnhandledExceptionsTest {

    @Test
    void check() {
        Exception e1 = new IllegalArgumentException("bad args");
        Exception e2 = new IllegalArgumentException("bad args 2");
        Exception e3 = new IllegalStateException("bad state");

        Operation o1 = new Operation(0, Operation.Type.INVOKE, FOO, 1);
        Operation o2 = new Operation(0, Operation.Type.INFO, FOO, 1, e1, List.of("Whoops!"));
        Operation o3 = new Operation(0, Operation.Type.INVOKE, FOO, 1);
        Operation o4 = new Operation(0, Operation.Type.INFO, FOO, 1, e2, List.of("Whoops", 2));
        Operation o5 = new Operation(0, Operation.Type.INVOKE, FOO, 1);
        Operation o6 = new Operation(0, Operation.Type.INFO, FOO, 1, e3, "oh-no");
        List<Operation> history = List.of(o1, o2, o3, o4, o5, o6);
        UnhandledExceptions unhandledExceptions = new UnhandledExceptions();
        System.out.println(e1.getClass());
        Result res = unhandledExceptions.check(null, history, new HashMap<>());
        Assertions.assertEquals(res.getValid(), true);
        Assertions.assertEquals(res.getExceptions().size(), 2);
        List<Map<String, Object>> exes=res.getExceptions();
        Map<String,Object> exe1=exes.get(0);
        Map<String,Object> exe2=exes.get(1);
        Assertions.assertEquals(exe1.get("class"),IllegalArgumentException.class);
        Assertions.assertEquals(exe1.get("count"),2);
    }
}