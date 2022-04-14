package core.checker.checker;

import com.google.common.collect.HashMultiset;
import core.checker.checker.Operation;
import core.checker.checker.Queue;
import core.checker.model.UnorderedQueue;
import core.checker.vo.Result;
import knossos.op.Op;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class QueueTest {

    /**
     * empty
     */
    @Test
    void check() {
        Queue queue = new Queue(null);
        Result result = queue.check(null, List.of(), Map.of());
        Assertions.assertEquals(result.getValid(), true);
    }

    @Test
    void check2() {
        Queue queue = new Queue(new UnorderedQueue(HashMultiset.create()));
        List<Operation> history = new ArrayList<>(List.of(Operation.invoke(1, Operation.F.ENQUEUE, 1)));
        Result result = queue.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), true);

    }

    @Test
    void check3() {
        Queue queue = new Queue(new UnorderedQueue(HashMultiset.create()));
        List<Operation> history = new ArrayList<>(List.of(Operation.ok(1, Operation.F.ENQUEUE, 1)));
        Result result = queue.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), true);

    }

    @Test
    void check4() {
        Queue queue = new Queue(new UnorderedQueue(HashMultiset.create()));
        Operation op1 = Operation.invoke(2, Operation.F.DEQUEUE, null);
        Operation op2 = Operation.invoke(1, Operation.F.ENQUEUE, 1);
        Operation op3 = Operation.ok(2, Operation.F.DEQUEUE, 1);
        List<Operation> history = new ArrayList<>(List.of(op1, op2, op3));
        Result result = queue.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), true);
    }

    @Test
    void check5(){
        Queue queue = new Queue(new UnorderedQueue(HashMultiset.create()));
        List<Operation> history = new ArrayList<>(List.of(Operation.ok(1, Operation.F.DEQUEUE, 1)));
        Result result = queue.check(null, history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), false);

    }

}