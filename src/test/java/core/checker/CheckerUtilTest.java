package core.checker;

import clojure.lang.Keyword;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.ClojureCaller;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CheckerUtilTest {
    CheckerUtil checkerUtil = new CheckerUtil();

    @Test
    void mergeValid() {
        List<Object> valids1 = List.of(true, true, false, Keyword.intern("unknown"));
        List<Object> valids2 = List.of(true, true, false, "unknown");
        Object res1 = ClojureCaller.call("jepsen.checker", "merge-valid", valids1);
        Object res2 = checkerUtil.mergeValid(valids2);
        Assertions.assertEquals(res1, res2);
    }

    @Test
    void mergeValid2() {
        List<Object> valids1 = List.of(true, true, Keyword.intern("unknown"));
        List<Object> valids2 = List.of(true, true, "unknown");
        Object res1 = ClojureCaller.call("jepsen.checker", "merge-valid", valids1);
        Object res2 = checkerUtil.mergeValid(valids2);
        Assertions.assertEquals(res1.toString().substring(1), res2.toString());
    }

    @Test
    void mergeValid3() {
        List<Object> valids1 = List.of(true, true);
        List<Object> valids2 = List.of(true, true);
        Object res1 = ClojureCaller.call("jepsen.checker", "merge-valid", valids1);
        Object res2 = checkerUtil.mergeValid(valids2);
        Assertions.assertEquals(res1,res2);
    }

    @Test
    void frequencyDistribution() {
        List<Double> points = List.of(0.3, 0d, 0.5, 0.65);
        List<Double> c = List.of(3d, 9d, 2.4, 999d);
        CheckerUtil checkerUtil = new CheckerUtil();
        Object res1 = ClojureCaller.call("jepsen.checker", "frequency-distribution", points, c);
        Object res2 = checkerUtil.frequencyDistribution(points, c);
        Assertions.assertEquals(res1, res2);
    }

    @Test
    void frequencyDistribution2() {
        List<Double> points = List.of(0.3, 0d, 0.5, 0.65);
        List<Double> c = List.of();
        CheckerUtil checkerUtil = new CheckerUtil();
        Map res = checkerUtil.frequencyDistribution(points, c);
        assertTrue(res.isEmpty());
    }


}