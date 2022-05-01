package core.checker.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PerfTest {

    @Test
    void broadenRange() {
        List<Double> range = List.of(3.14, 3.14);
        List<Double> res = Perf.broadenRange(3.14, 3.14);
        List<Double> resExpected = List.of(2.14, 4.140000000000001);
        Assertions.assertEquals(resExpected, res);
    }

    @Test
    void broadenRange2() {
        List<Double> range = List.of(3.14, 5.14);
        List<Double> res = Perf.broadenRange(3.14, 5.14);
        List<Double> resExpected = List.of(3.1, 5.2);
        Assertions.assertEquals(resExpected, res);
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
        double res = Perf.bucketScale(dt, b);
        Assertions.assertEquals(res, 25);
    }

    @Test
    void bucketTime() {
        long dt = 10, b = 2;
        double res = Perf.bucketTime(dt, b);
        Assertions.assertEquals(res, 5);
    }

    @Test
    void buckets() {
        long dt = 10, b = 30;
        List<Double> res = Perf.buckets(dt, b);
        List<Double> resExpected = List.of(5d, 15d, 25d);
        Assertions.assertEquals(resExpected, res);
    }

    @Test
    void bucketPoints() {
        long dt = 10;
        List<List<?>> points = List.of(List.of(33L, 1), List.of(36L, 1), List.of(68L, 2));
        Map<Double, List<List<?>>> res = new HashMap<>(Perf.bucketPoints(dt, points));
        Map<Double, List<List<?>>> resExpected = new HashMap<>(Map.of(
                65d, new ArrayList<>(List.of(new ArrayList<>(List.of(68, 2)))),
                35d, new ArrayList<>(List.of(new ArrayList<>(List.of(33, 1)), new ArrayList<>(List.of(36, 1))))
        ));
        Assertions.assertEquals(res.keySet(), resExpected.keySet());
        for (Map.Entry<Double, List<List<?>>> entry : res.entrySet()) {
            Assertions.assertEquals(resExpected.get(entry.getKey()).size(), entry.getValue().size());
        }
    }

    @Test
    void quantiles() {
        List<Double> quantiles = List.of(0.33, 0.14, 0.55);
        List<Double> points = List.of(33d, 1d, 36d, 1d, 68d, 2d);
        Map<Double, Double> res = Perf.quantiles(quantiles, points);
        Map<Double, Double> resExpected = new HashMap<>(Map.of(
                0.33, 1.0, 0.14, 1.0, 0.55, 33.0
        ));
        Assertions.assertEquals(res, resExpected);
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