package core.checker.checker;

import core.checker.linearizability.History;
import core.checker.vo.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

class SetFullTest {

    List<Operation> history(List<Operation> h) {
        History.index(h);
        int count = h.size();
        if (count == 0) {
            return h;
        } else if (count == 1) {
            h.get(0).setTime(0);
            return h;
        } else {
            h.get(0).setTime(0);
            List<Operation> res = new ArrayList<>();
            for (Operation op : h) {
                op.setTime(h.get(0).getTime() + 1000000);
                res.add(op);
            }
            return res;
        }
    }

    @Test
    void neverRead() {
        Operation op1 = Operation.invoke(0, Operation.F.ADD, 0);
        Operation op2 = Operation.ok(0, Operation.F.ADD, 0);
        List<Operation> history = List.of(op1, op2);
        SetFull setFull = new SetFull(new HashMap<>());
        Result result = setFull.check(null, history(history),new HashMap<>());
        Map<String,Object> res=result.getRes();
        Assertions.assertEquals(res.get("lost"),new ArrayList<>());
        Assertions.assertEquals(res.get("attempt-count"),1);
        Assertions.assertEquals(res.get("lost-count"),0);
        Assertions.assertEquals(res.get("never-read"),List.of(0));
        Assertions.assertEquals(res.get("never-read-count"),1);
        Assertions.assertEquals(res.get("stale-count"),0);
        Assertions.assertEquals(res.get("stale"),new ArrayList<>());
        Assertions.assertEquals(res.get("worst-stale"),new ArrayList<>());
        Assertions.assertEquals(res.get("stable-count"),0);
        Assertions.assertEquals(res.get("duplicated-count"),0);
        Assertions.assertEquals(res.get("duplicated"),new HashMap<>());
        Assertions.assertEquals(res.get("valid?"),"unknown");
    }

    @Test
    void neverConfirmed() {
        Operation op1 = Operation.invoke(0, Operation.F.ADD, 0);
        Operation op2 = Operation.ok(0, Operation.F.ADD, 0);
        Operation op3 = Operation.invoke(1, Operation.F.READ, null);
        Operation op4 = Operation.ok(1, Operation.F.READ, new HashSet<>(List.of(0)));
        Operation op5 = Operation.ok(1, Operation.F.READ, new HashSet<>());
        List<Operation> history = List.of(op1, op2,op3,op4,op5);
        SetFull setFull = new SetFull(new HashMap<>());
        Result result = setFull.check(null, history(history),new HashMap<>());
        Map<String,Object> res=result.getRes();
        Assertions.assertEquals(res.get("lost"),new ArrayList<>());
        Assertions.assertEquals(res.get("attempt-count"),1);
        Assertions.assertEquals(res.get("lost-count"),0);
        Assertions.assertEquals(res.get("never-read"),List.of(0));
        Assertions.assertEquals(res.get("never-read-count"),1);
        Assertions.assertEquals(res.get("stale-count"),0);
        Assertions.assertEquals(res.get("stale"),new ArrayList<>());
        Assertions.assertEquals(res.get("worst-stale"),new ArrayList<>());
        Assertions.assertEquals(res.get("stable-count"),0);
        Assertions.assertEquals(res.get("duplicated-count"),0);
        Assertions.assertEquals(res.get("duplicated"),new HashMap<>());
        Assertions.assertEquals(res.get("valid?"),"unknown");
    }


}