package util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

class UtilTest {

    @Test
    void integerIntervalSetStr() {
        Set<Integer> test = Set.of(1, 0, 3, 10);
        String res = Util.integerIntervalSetStr(test);
        String resExpected = "#{0..1 3 10}";
        Assertions.assertEquals(res, resExpected);
    }

    @Test
    void integerIntervalSetStr2() {
        Set<Integer> test = Set.of();
        String res = Util.integerIntervalSetStr(test);
        String resExpected = "#{}";
        Assertions.assertEquals(resExpected, res);
    }

    @Test
    void nanos2ms() {
        double nanos = 1.5e10;
        double res = Util.nanos2ms(nanos);
        double resExpected = 15000.0;
        Assertions.assertEquals(res, resExpected);
    }

    @Test
    void nanos2secs() {
        double nanos = 1.5e10;
        double res = Util.nanos2secs(nanos);
        double resExpected = 15.0;
        Assertions.assertEquals(res, resExpected);
    }


    //    @Test
    //    void mapVals() {
    //        IFn inc = Clojure.var("clojure.core", "inc");
    //        Map map = Map.of(1, 2, 3, 4, 5, 7);
    //        Function<Object, Object> func = (k) -> (int) k + 1;
    //
    //        Map res = (Map) ClojureCaller.call("jepsen.util", "map-vals", inc, map);
    //        Map res2 = Util.mapVals(func, map);
    //        Assertions.assertEquals(res.size(), res2.size());
    //        System.out.println(res2.get(6));
    //        res2.forEach((k, v) -> Assertions.assertEquals(Integer.parseInt(res.get(k).toString()), v));
    //
    //    }

    //    @Test
    //    void mapKv() {
    //        Map map = Map.of(1, 2, 3, 4, 5, 7);
    //        Function<Object[], Object[]> func = (k) -> new Object[]{(int) k[0] + 1, (int) k[1] + 1};
    //        Map res = Util.mapKv(func, map);
    //        Assertions.assertFalse(res.containsKey(1));
    //        Assertions.assertTrue(res.containsKey(6));
    //        Assertions.assertEquals(res.get(6), 8);
    //    }

    @Test
    void longestCommonPrefix() {
        List<List<String>> test = List.of(List.of("haze", "had", "haze"), List.of("haze", "had", "haze"), List.of("haze", "had", "wow"));
        List<String> res = Util.longestCommonPrefix(test);
        List<String> resExpected = List.of("haze", "had");
        Assertions.assertEquals(resExpected, res);
    }

    @Test
    void dropCommonProperPrefix() {
        List<List<String>> test = List.of(List.of("haze", "had", "haze"), List.of("haze", "had", "haze"), List.of("haze", "had", "wow"));
        List<List<String>> res = Util.dropCommonProperPrefix(test);
        List<List<String>> resExpected = List.of(List.of("haze"), List.of("haze"), List.of("wow"));
        Assertions.assertEquals(resExpected, res);
    }
}