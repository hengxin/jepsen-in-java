package core.checker.util;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import core.checker.checker.CheckerUtil;
import net.logstash.logback.encoder.com.lmax.disruptor.LifecycleAware;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.ClojureCaller;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PerfTest {

    @Test
    void broadenRange() {
        List<Double> range = List.of(3.14, 3.14);
        List<Double> res1 = (List<Double>) ClojureCaller.call("jepsen.checker.perf", "broaden-range", range);
        List<Double> res2 = Perf.broadenRange(3.14, 3.14);
        Assertions.assertEquals(res1.size(), res2.size());
        for (Double d : res1) {
            Assertions.assertTrue(res2.contains(d));
        }

    }

    @Test
    void broadenRange2() {
        List<Double> range = List.of(3.14, 5.14);
        List<Double> res1 = (List<Double>) ClojureCaller.call("jepsen.checker.perf", "broaden-range", range);
        List<Double> res2 = Perf.broadenRange(3.14, 5.14);
        Assertions.assertEquals(res1.size(), res2.size());
        for (Double d : res1) {
            Assertions.assertTrue(res2.contains(d));
        }

    }


    @Test
    void broadenRange3() {
        List<List<Double>> identicalPoints = List.of(List.of(
                        0d, 0d, -1d, 1d
                ), List.of(
                        -1d, -1d, -2d, 0d
                ), List.of(
                        4d, 4d, 3d, 5d
                )
        );
        List<List<Double>> normalIntegers = List.of(List.of(
                0d, 1d, 0.0, 1d
        ), List.of(
                1d, 2d, 1.0, 2d
        ), List.of(
                9d, 10d, 9.0, 10d
        ), List.of(
                0d, 10d, 0.0, 10d
        ));
        List<List<Double>> biggerInteger = List.of(List.of(
                        1000d, 10000d, 1000.0, 10000d
                ), List.of(
                        1234d, 5678d, 1000.0, 6000.0
                ), List.of(
                        4d, 4d, 3d, 5d
                )
        );
        List<List<Double>> tinyNumbers = List.of(
                List.of(
                        0.03415, 0.03437, 0.034140000000000004, 0.034370000000000005
                )
        );

        for (List<Double> i : identicalPoints) {
            Assertions.assertEquals(Perf.broadenRange(i.get(0), i.get(1)), i.subList(2, 4));
        }

        for (List<Double> i : normalIntegers) {
            Assertions.assertEquals(Perf.broadenRange(i.get(0), i.get(1)), i.subList(2, 4));
        }
        for (List<Double> i : biggerInteger) {
            Assertions.assertEquals(Perf.broadenRange(i.get(0), i.get(1)), i.subList(2, 4));
        }
        for (List<Double> i : tinyNumbers) {
            Assertions.assertEquals(Perf.broadenRange(i.get(0), i.get(1)), i.subList(2, 4));
        }

    }

    @Test
    void bucketScale() {
        long dt = 10, b = 2;
        Object res1 = ClojureCaller.call("jepsen.checker.perf", "bucket-scale", dt, b);
        double res2 = Perf.bucketScale(dt, b);
        Assertions.assertEquals(Double.parseDouble(res1.toString()), res2);
    }

    @Test
    void bucketTime() {
        long dt = 10, b = 2;
        long res1 = (long) ClojureCaller.call("jepsen.checker.perf", "bucket-time", dt, b);
        double res2 = Perf.bucketTime(dt, b);
        Assertions.assertEquals(res1, res2);
    }

    @Test
    void buckets() {
        long dt = 10, b = 30;
        List<Long> res1 = (List<Long>) ClojureCaller.call("jepsen.checker.perf", "buckets", dt, b);
        List<Double> res2 = Perf.buckets(dt, b);
        Assertions.assertEquals(res1.size(), res2.size());
    }

    @Test
    void bucketPoints() {
        long dt = 10;
        List<List<?>> points = List.of(List.of(33L, 1), List.of(36L, 1), List.of(68L, 2));
        Map<Long, List> res1 = (Map<Long, List>) ClojureCaller.call("jepsen.checker.perf", "bucket-points", dt, points);
        Map<Double, List<List<?>>> res2 = Perf.bucketPoints(dt, points);
        Assertions.assertEquals(res1.size(), res2.size());
        for (Long key : res1.keySet()) {
            Assertions.assertEquals(res1.get(key), res2.get(key));
        }
    }

    @Test
    void quantiles() {
        List<Double> quantiles = List.of(0.33, 0.14, 0.55);
        List<Double> points = List.of(33d, 1d, 36d, 1d, 68d, 2d);
        Map<Double, Double> res1 = (Map) ClojureCaller.call("jepsen.checker.perf", "quantiles", quantiles, points);
        Map<Double, Double> res2 = Perf.quantiles(quantiles, points);
        Assertions.assertEquals(res1.size(), res2.size());
        for (Double key : res1.keySet()) {
            Assertions.assertEquals(res1.get(key), res2.get(key));
        }
    }

    @Test
    void testBucketPoints() {
        List<List<?>> points = List.of(
                List.of(1, "a"),
                List.of(7, "g"),
                List.of(5, "e"),
                List.of(2, "b"),
                List.of(3, "c"),
                List.of(4, "d"),
                List.of(6, "f")
        );
        Map<Double,List<List<?>>> res=Perf.bucketPoints(2, points);
        Assertions.assertEquals(res,Map.of(
                1L,List.of(List.of(1,"a")),
                3L,List.of(List.of(2,"b"),List.of(3,"c")),
                5L,List.of(List.of(5,"e"),List.of(4,"d")),
                7L,List.of(List.of(7,"g"),List.of(6,"f"))
        ));

    }

    @Test
    void latencies2quantiles() {
        Map<Double,List<List<?>>> res=Perf.latencies2quantiles(5,List.of(0d,1d),List.of(
                List.of(0,0),
                List.of(1,10),
                List.of(2,1),
                List.of(3,1),
                List.of(4,1),
                List.of(5,20),
                List.of(6,21),
                List.of(7,22),
                List.of(8,25),
                List.of(9,25),
                List.of(10,25)
        ));
        Map<?,List<List<?>>> expected=Map.of(
                0,List.of(List.of(5.0/2,0d),List.of(15.0/2,20d),List.of(25.0/2,25d)),
                1,List.of(List.of(5.0/2,10d),List.of(15.0/2,25d),List.of(25.0/2,25d))

        );
        Assertions.assertEquals(res.get(0d),expected.get(0));
        Assertions.assertEquals(res.get(1d),expected.get(1));
    }


}