package core.checker.checker;

import core.checker.vo.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class CounterTest {

    /**
     * empty
     */
    @Test
    void check() {
        Counter counter = new Counter();
        Result result = counter.check(null, new ArrayList<>(), new HashMap<>());
        Assertions.assertEquals(result.getValid(), true);
    }

    @Test
    void check2() {
        Counter counter = new Counter();
        Operation op1 = Operation.invoke(0, Operation.F.READ, null);
        Operation op2 = Operation.ok(0, Operation.F.READ, 0);
        List<Operation> history = new ArrayList<>(List.of(op1, op2));
        Result result = counter.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), true);
    }

    /**
     * ignore failed ops
     */
    @Test
    void check3() {
        Counter counter = new Counter();
        Operation op1 = Operation.invoke(0, Operation.F.ADD, 1);
        Operation op2 = Operation.fail(0, Operation.F.ADD, 1);
        Operation op3 = Operation.invoke(0, Operation.F.READ, null);
        Operation op4 = Operation.ok(0, Operation.F.READ, 0);
        List<Operation> history = new ArrayList<>(List.of(op1, op2, op3, op4));
        Result result = counter.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), true);
    }

    /**
     * initial invalid read
     */
    @Test
    void check4() {
        Counter counter = new Counter();
        Operation op1 = Operation.invoke(0, Operation.F.READ, null);
        Operation op2 = Operation.ok(0, Operation.F.READ, 1);
        List<Operation> history = new ArrayList<>(List.of(op1, op2));
        Result result = counter.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), false);

    }

    @Test
    void check5() {
        Counter counter = new Counter();
        Operation op1 = Operation.invoke(0, Operation.F.READ, null);
        Operation op2 = Operation.invoke(1, Operation.F.ADD, 1);
        Operation op3 = Operation.invoke(2, Operation.F.READ, null);
        Operation op4 = Operation.invoke(3, Operation.F.ADD, 2);
        Operation op5 = Operation.invoke(4, Operation.F.READ, null);
        Operation op6 = Operation.invoke(5, Operation.F.ADD, 4);
        Operation op7 = Operation.invoke(6, Operation.F.READ, null);
        Operation op8 = Operation.invoke(7, Operation.F.ADD, 8);
        Operation op9 = Operation.invoke(8, Operation.F.READ, null);
        Operation op10 = Operation.ok(0, Operation.F.READ, 6);
        Operation op11 = Operation.ok(1, Operation.F.ADD, 1);
        Operation op12 = Operation.ok(2, Operation.F.READ, 0);
        Operation op13 = Operation.ok(3, Operation.F.ADD, 2);
        Operation op14 = Operation.ok(4, Operation.F.READ, 3);
        Operation op15 = Operation.ok(5, Operation.F.ADD, 4);
        Operation op16 = Operation.ok(6, Operation.F.READ, 100);
        Operation op17 = Operation.ok(7, Operation.F.ADD, 8);
        Operation op18 = Operation.ok(8, Operation.F.READ, 15);
        List<Operation> history = new ArrayList<>(List.of(op1, op2,op3,op4,op5,op6,op7,op8,op9,op10,op11,op12,op13,op14,op15,op16,op17,op18));
        Result result = counter.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), false);
    }

    @Test
    void check6() {
        Counter counter = new Counter();
        Operation op1 = Operation.invoke(0, Operation.F.READ, null);
        Operation op2 = Operation.invoke(1, Operation.F.ADD, 1);
        Operation op3 = Operation.ok(0, Operation.F.READ, 0);
        Operation op4 = Operation.invoke(0, Operation.F.READ, null);
        Operation op5 = Operation.ok(1, Operation.F.ADD, 1);
        Operation op6 = Operation.invoke(1, Operation.F.ADD, 2);
        Operation op7 = Operation.ok(0, Operation.F.READ, 3);
        Operation op8 = Operation.invoke(0, Operation.F.READ, null);
        Operation op9 = Operation.ok(1, Operation.F.ADD, 2);
        Operation op10 = Operation.ok(0, Operation.F.READ, 5);
        List<Operation> history = new ArrayList<>(List.of(op1, op2,op3,op4,op5,op6,op7,op8,op9,op10));
        Result result = counter.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), false);
    }



}