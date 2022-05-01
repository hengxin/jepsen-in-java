package core.checker.checker;

import core.checker.linearizability.History;
import core.checker.vo.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

class SetFullTest {
    Operation a = Operation.invoke(0, Operation.F.ADD, 0);
    Operation newA = Operation.ok(0, Operation.F.ADD, 0);
    Operation r = Operation.invoke(1, Operation.F.READ, null);
    Operation readPlus = Operation.ok(1, Operation.F.READ, new HashSet<>(List.of(0)));
    Operation readMinus = Operation.ok(1, Operation.F.READ, new HashSet<>());
    Operation a0 = Operation.invoke(0, Operation.F.ADD, 0);
    Operation newA0 = Operation.ok(0, Operation.F.ADD, 0);
    Operation a1 = Operation.invoke(1, Operation.F.ADD, 1);
    Operation newA1 = Operation.ok(1, Operation.F.ADD, 1);
    Operation r2 = Operation.invoke(2, Operation.F.READ, null);
    Operation r3 = Operation.invoke(3, Operation.F.READ, null);
    Operation newR2 = Operation.ok(2, Operation.F.READ, new ArrayList<>());
    Operation newR3 = Operation.ok(3, Operation.F.READ, new ArrayList<>());
    Operation newR20 = Operation.ok(2, Operation.F.READ, List.of(0));
    Operation newR30 = Operation.ok(3, Operation.F.READ, List.of(0));
    Operation newR21 = Operation.ok(2, Operation.F.READ, List.of(1));
    Operation newR31 = Operation.ok(3, Operation.F.READ, List.of(1));
    Operation newR201 = Operation.ok(2, Operation.F.READ, List.of(0, 1));
    Operation newR301 = Operation.ok(3, Operation.F.READ, List.of(0, 1));

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
            List<Operation> res = new ArrayList<>(List.of(h.get(0)));
            for (int i = 1; i < h.size(); i++) {
                Operation op = h.get(i);
                op.setTime(res.get(res.size() - 1).getTime() + 1000000);
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
        SetFull setFull = new SetFull();
        Result result = setFull.check(null, history(history), new HashMap<>());
        Map<String, Object> res = result.getRes();
        Assertions.assertEquals(res.get("lost"), new ArrayList<>());
        Assertions.assertEquals(res.get("attempt-count"), 1);
        Assertions.assertEquals(res.get("lost-count"), 0);
        Assertions.assertEquals(res.get("never-read"), List.of(0));
        Assertions.assertEquals(res.get("never-read-count"), 1);
        Assertions.assertEquals(res.get("stale-count"), 0);
        Assertions.assertEquals(res.get("stale"), new ArrayList<>());
        Assertions.assertEquals(res.get("worst-stale"), new ArrayList<>());
        Assertions.assertEquals(res.get("stable-count"), 0);
        Assertions.assertEquals(res.get("duplicated-count"), 0);
        Assertions.assertEquals(res.get("duplicated"), new HashMap<>());
        Assertions.assertEquals(res.get("valid?"), "unknown");
    }

    @Test
    void neverConfirmed() {
        List<Operation> history = List.of(a, r, readMinus);
        SetFull setFull = new SetFull();
        Result result = setFull.check(null, history(history), new HashMap<>());
        Map<String, Object> res = result.getRes();
        Assertions.assertEquals(res.get("lost"), new ArrayList<>());
        Assertions.assertEquals(res.get("attempt-count"), 1);
        Assertions.assertEquals(res.get("lost-count"), 0);
        Assertions.assertEquals(res.get("never-read"), List.of(0));
        Assertions.assertEquals(res.get("never-read-count"), 1);
        Assertions.assertEquals(res.get("stale-count"), 0);
        Assertions.assertEquals(res.get("stale"), new ArrayList<>());
        Assertions.assertEquals(res.get("worst-stale"), new ArrayList<>());
        Assertions.assertEquals(res.get("stable-count"), 0);
        Assertions.assertEquals(res.get("duplicated-count"), 0);
        Assertions.assertEquals(res.get("duplicated"), new HashMap<>());
        Assertions.assertEquals(res.get("valid?"), "unknown");
    }

    @Test
    void SuccessfullyRead() {
        List<Operation> history1 = List.of(r, a, readPlus, newA);
        List<Operation> history2 = List.of(r, a, newA, readPlus);
        List<Operation> history3 = List.of(a, r, readPlus, newA);
        List<Operation> history4 = List.of(a, r, newA, readPlus);
        List<Operation> history5 = List.of(a, newA, r, readPlus);
        SetFull setFull = new SetFull();
        Result result1 = setFull.check(null, history(history1), new HashMap<>());
        Result result2 = setFull.check(null, history(history2), new HashMap<>());
        Result result3 = setFull.check(null, history(history3), new HashMap<>());
        Result result4 = setFull.check(null, history(history4), new HashMap<>());
        Result result5 = setFull.check(null, history(history5), new HashMap<>());
        List<Result> results = List.of(result1, result2, result3, result4, result5);
        for (Result result : results) {
            Map<String, Object> res = result.getRes();
            Assertions.assertEquals(res.get("lost"), new ArrayList<>());
            Assertions.assertEquals(res.get("attempt-count"), 1);
            Assertions.assertEquals(res.get("lost-count"), 0);
            Assertions.assertEquals(res.get("never-read"), new ArrayList<>());
            Assertions.assertEquals(res.get("never-read-count"), 0);
            Assertions.assertEquals(res.get("stale-count"), 0);
            Assertions.assertEquals(res.get("stale"), new ArrayList<>());
            Assertions.assertEquals(res.get("worst-stale"), new ArrayList<>());
            Assertions.assertEquals(res.get("stable-count"), 1);
            Assertions.assertEquals(res.get("stable-latencies"), Map.of(0d, 0d, 0.5, 0d, 0.95, 0d, 0.99, 0d, 1d, 0d));
            Assertions.assertEquals(res.get("duplicated-count"), 0);
            Assertions.assertEquals(res.get("duplicated"), new HashMap<>());
            Assertions.assertEquals(res.get("valid?"), true);
        }

    }

    @Test
    void absentReadAfter() {
        List<Operation> history = List.of(a, newA, r, readMinus);
        SetFull setFull = new SetFull();
        Result result = setFull.check(null, history(history), new HashMap<>());
        Map<String, Object> res = result.getRes();
        Assertions.assertEquals(res.get("lost"), List.of(0));
        Assertions.assertEquals(res.get("attempt-count"), 1);
        Assertions.assertEquals(res.get("lost-count"), 1);
        Assertions.assertEquals(res.get("never-read"), new ArrayList<>());
        Assertions.assertEquals(res.get("never-read-count"), 0);
        Assertions.assertEquals(res.get("stale-count"), 0);
        Assertions.assertEquals(res.get("stale"), new ArrayList<>());
        Assertions.assertEquals(res.get("worst-stale"), new ArrayList<>());
        Assertions.assertEquals(res.get("stable-count"), 0);
        Assertions.assertEquals(res.get("lost-latencies"), Map.of(0d, 0d, 0.5, 0d, 0.95, 0d, 0.99, 0d, 1d, 0d));
        Assertions.assertEquals(res.get("duplicated-count"), 0);
        Assertions.assertEquals(res.get("duplicated"), new HashMap<>());
        Assertions.assertEquals(res.get("valid?"), false);
    }

    @Test
    void absentReadConcurrently() {
        List<Operation> history1 = List.of(r, a, readMinus, newA);
        List<Operation> history2 = List.of(r, a, newA, readMinus);
        List<Operation> history3 = List.of(a, r, readMinus, newA);
        List<Operation> history4 = List.of(a, r, newA, readMinus);
        SetFull setFull = new SetFull();
        Result result1 = setFull.check(null, history(history1), new HashMap<>());
        Result result2 = setFull.check(null, history(history2), new HashMap<>());
        Result result3 = setFull.check(null, history(history3), new HashMap<>());
        Result result4 = setFull.check(null, history(history4), new HashMap<>());
        List<Result> results = List.of(result1, result2, result3, result4);
        for (Result result : results) {
            Map<String, Object> res = result.getRes();
            Assertions.assertEquals(res.get("lost"), new ArrayList<>());
            Assertions.assertEquals(res.get("attempt-count"), 1);
            Assertions.assertEquals(res.get("lost-count"), 0);
            Assertions.assertEquals(res.get("never-read"), List.of(0));
            Assertions.assertEquals(res.get("never-read-count"), 1);
            Assertions.assertEquals(res.get("stale-count"), 0);
            Assertions.assertEquals(res.get("stale"), new ArrayList<>());
            Assertions.assertEquals(res.get("worst-stale"), new ArrayList<>());
            Assertions.assertEquals(res.get("stable-count"), 0);
            Assertions.assertEquals(res.get("duplicated-count"), 0);
            Assertions.assertEquals(res.get("duplicated"), new HashMap<>());
            Assertions.assertEquals(res.get("valid?"), "unknown");
        }


    }

    @Test
    void writePresentAndMissing() {
        List<Operation> history = List.of(a0, a1, r2, newR21, newA0, newA1, new Operation(r2), newR201, new Operation(r2), newR20, new Operation(r2), newR2);
        SetFull setFull = new SetFull();
        Result result = setFull.check(null, history(history), new HashMap<>());
        Map<String, Object> res = result.getRes();
        Assertions.assertEquals(res.get("lost"), List.of(0, 1));
        Assertions.assertEquals(res.get("attempt-count"), 2);
        Assertions.assertEquals(res.get("lost-count"), 2);
        Assertions.assertEquals(res.get("never-read"), new ArrayList<>());
        Assertions.assertEquals(res.get("never-read-count"), 0);
        Assertions.assertEquals(res.get("stale-count"), 0);
        Assertions.assertEquals(res.get("stale"), new ArrayList<>());
        Assertions.assertEquals(res.get("worst-stale"), new ArrayList<>());
        Assertions.assertEquals(res.get("stable-count"), 0);
        Assertions.assertEquals(res.get("lost-latencies"), Map.of(0d, 3d, 0.5, 4d, 0.95, 4d, 0.99, 4d, 1d, 4d));
        Assertions.assertEquals(res.get("duplicated-count"), 0);
        Assertions.assertEquals(res.get("duplicated"), new HashMap<>());
        Assertions.assertEquals(res.get("valid?"), false);
    }

    @Test
    void writeFlutterAndStableOrLost() {
        List<Operation> history = List.of(a0, newA0, a1, r2, newR21, newA1, new Operation(r2), r3, newR31, newR20);
        SetFull setFull = new SetFull();
        Result result = setFull.check(null, history(history), new HashMap<>());
        Map<String, Object> res = result.getRes();
        Assertions.assertEquals(res.get("lost"), List.of(0));
        Assertions.assertEquals(res.get("attempt-count"), 2);
        Assertions.assertEquals(res.get("lost-count"), 1);
        Assertions.assertEquals(res.get("never-read"), new ArrayList<>());
        Assertions.assertEquals(res.get("never-read-count"), 0);
        Assertions.assertEquals(res.get("stale-count"), 1);
        Assertions.assertEquals(res.get("stale"), List.of(1));
        List<?> worstStales = (List<?>) res.get("worst-stale");
        Map<?, ?> worstStale = (Map<?, ?>) worstStales.get(0);
        Assertions.assertEquals(worstStale.get("element"), 1);
        Assertions.assertEquals(worstStale.get("known"), Optional.of(newR21));
        r2.setIndex(6);
        Assertions.assertEquals(worstStale.get("last-absent"), r2);
        Assertions.assertEquals(worstStale.get("lost-latency"), Double.MAX_VALUE);
        Assertions.assertEquals(worstStale.get("outcome"), "stable");
        Assertions.assertEquals(worstStale.get("stable-latency"), 2d);
        Assertions.assertEquals(res.get("stable-count"), 1);
        Assertions.assertEquals(res.get("lost-latencies"), Map.of(0d, 5d, 0.5, 5d, 0.95, 5d, 0.99, 5d, 1d, 5d));
        Assertions.assertEquals(res.get("stable-latencies"), Map.of(0d, 2d, 0.5, 2d, 0.95, 2d, 0.99, 2d, 1d, 2d));
        Assertions.assertEquals(res.get("duplicated-count"), 0);
        Assertions.assertEquals(res.get("duplicated"), new HashMap<>());
        Assertions.assertEquals(res.get("valid?"), false);
    }



}