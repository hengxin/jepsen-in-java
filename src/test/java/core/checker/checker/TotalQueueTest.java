package core.checker.checker;

import core.checker.vo.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TotalQueueTest {

    /**
     * empty
     */
    @Test
    void check() {
        TotalQueue totalQueue = new TotalQueue();
        Result result = totalQueue.check(null, new ArrayList<>(), new HashMap<>());
        Assertions.assertEquals(result.getValid(), true);
    }


    /**
     * right
     */
    @Test
    void check2() {
        Operation op1 = Operation.invoke(1, Operation.F.ENQUEUE, 1);
        Operation op2 = Operation.invoke(2, Operation.F.ENQUEUE, 2);
        Operation op3 = Operation.ok(2, Operation.F.ENQUEUE, 2);
        Operation op4 = Operation.invoke(3, Operation.F.DEQUEUE, 1);
        Operation op5 = Operation.ok(3, Operation.F.DEQUEUE, 1);
        Operation op6 = Operation.invoke(3, Operation.F.DEQUEUE, 2);
        Operation op7 = Operation.ok(3, Operation.F.DEQUEUE, 2);
        TotalQueue totalQueue = new TotalQueue();
        List<Operation> history = new ArrayList<>(List.of(op1, op2, op3, op4, op5, op6, op7));
        Result result = totalQueue.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), true);
    }


    /**
     * error
     */
    @Test
    void check3() {
        Operation op1 = Operation.invoke(1, Operation.F.ENQUEUE, "hung");
        Operation op2 = Operation.invoke(2, Operation.F.ENQUEUE, "enqueued");
        Operation op3 = Operation.ok(2, Operation.F.ENQUEUE, "enqueued");
        Operation op4 = Operation.invoke(3, Operation.F.ENQUEUE, "dup");
        Operation op5 = Operation.ok(3, Operation.F.ENQUEUE, "dup");
        Operation op6 = Operation.invoke(4, Operation.F.DEQUEUE, null);
        Operation op7 = Operation.invoke(5, Operation.F.DEQUEUE, null);
        Operation op8 = Operation.ok(5, Operation.F.DEQUEUE, "w");
        Operation op9 = Operation.invoke(6, Operation.F.DEQUEUE, null);
        Operation op10 = Operation.ok(6, Operation.F.DEQUEUE, "dup");
        Operation op11 = Operation.invoke(7, Operation.F.DEQUEUE, null);
        Operation op12 = Operation.ok(7, Operation.F.DEQUEUE, "dup");
        TotalQueue totalQueue = new TotalQueue();
        List<Operation> history = new ArrayList<>(List.of(op1, op2, op3, op4, op5, op6, op7, op8, op9, op10, op11, op12));
        Result result = totalQueue.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), false);
    }

}