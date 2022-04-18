package core.checker.checker;

import com.jcraft.jsch.MAC;
import core.checker.vo.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RateGraphTest {


    public List<Operation> history;

    RateGraphTest() {
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
    void check() {
        RateGraph rateGraph = new RateGraph(new HashMap<>());
        Result result = rateGraph.check(new HashMap(Map.of(
                "name", "rate graph",
                "start-time", 0
        )), this.history, new HashMap<>());
        Assertions.assertEquals(result.getValid(), true);
    }
}