package core.checker.checker;

import core.checker.vo.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CheckerUtilTest {
    CheckerUtil checkerUtil = new CheckerUtil();
    public List<Operation> history;

    CheckerUtilTest() {
        Random random = new Random();
        List<Operation> history = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            double latency = 1e9 / (random.nextInt(1000) + 1);
            history.addAll(perfGen(latency));
        }
        this.history = history;
    }

    private List<Operation> perfGen(double latency) {
        return perfGen(latency, null);
    }

    private List<Operation> perfGen(double latency, Object nemesis) {
        Random random = new Random();
        int i = random.nextInt(2);
        List<Operation.F> fs = List.of(Operation.F.WRITE, core.checker.checker.Operation.F.READ);
        Operation.F f = fs.get(i);
        int proc = random.nextInt(100);
        double time = 1e9 * random.nextInt(100);
        i = random.nextInt(8);
        List<Operation.Type> types = List.of(Operation.Type.OK, Operation.Type.OK, Operation.Type.OK, Operation.Type.OK, Operation.Type.OK,
                Operation.Type.FAIL, Operation.Type.INFO, Operation.Type.INFO);
        Operation.Type type = types.get(i);
        Operation op1 = new Operation();
        op1.setProcess(proc);
        op1.setType(Operation.Type.INVOKE);
        op1.setF(f);
        op1.setTime(time);
        Operation op2 = new Operation();
        op2.setProcess(proc);
        op2.setType(type);
        op2.setF(f);
        op2.setTime(time + latency);
        return new ArrayList<>(List.of(op1, op2));
    }

    @Test
    void mergeValid() {
        List<Object> valids2 = List.of(true, true, false, "unknown");
        Object res = checkerUtil.mergeValid(valids2);
        Assertions.assertEquals(res, false);
    }

    @Test
    void mergeValid2() {
        List<Object> valids2 = List.of(true, true, "unknown");
        Object res = checkerUtil.mergeValid(valids2);
        Assertions.assertEquals(res, "unknown");
    }

    @Test
    void mergeValid3() {
        List<Object> valids1 = List.of(true, true);
        List<Object> valids2 = List.of(true, true);
        Object res = checkerUtil.mergeValid(valids2);
        Assertions.assertEquals(res,true);
    }

    @Test
    void frequencyDistribution() {
        List<Double> points = List.of(0.3, 0d, 0.5, 0.65);
        List<Double> c = List.of(3d, 9d, 2.4, 999d);
        CheckerUtil checkerUtil = new CheckerUtil();
        Object res = checkerUtil.frequencyDistribution(points, c);
        Map<Double,Double> resExpected= Map.of(
                0.0,2.4,
                0.3,3.0,
                0.5,9.0,
                0.65,9.0
        );
        Assertions.assertEquals(res, resExpected);

    }

    @Test
    void frequencyDistribution2() {
        List<Double> points = List.of(0.3, 0d, 0.5, 0.65);
        List<Double> c = List.of();
        CheckerUtil checkerUtil = new CheckerUtil();
        Map res = checkerUtil.frequencyDistribution(points, c);
        assertTrue(res.isEmpty());
    }

    @Test
    void perf() {
        Checker checker = CheckerUtil.perf(new HashMap<>());
        Result result = checker.check(new HashMap<>(Map.of(
                "name", "perf graph",
                "start-time", 0
        )), history, new HashMap<>());

        Map<?, ?> results = result.getResults();
        Result latencyGraph = (Result) results.get("latency-graph");
        Result rateGraph = (Result) results.get("rate-graph");
        Assertions.assertEquals(result.getValid(), true);
        Assertions.assertEquals(latencyGraph.getValid(), true);
        Assertions.assertEquals(rateGraph.getValid(), true);
    }

}