package util;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
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
        String res1 = ClojureCaller.call("jepsen.util", "integer-interval-set-str", test).toString();
        String res2 = Util.integerIntervalSetStr(test);
        Assertions.assertEquals(res1, res2);
    }

    @Test
    void integerIntervalSetStr2() {
        Set<Integer> test = Set.of();
        String res1 = ClojureCaller.call("jepsen.util", "integer-interval-set-str", test).toString();
        String res2 = Util.integerIntervalSetStr(test);
        Assertions.assertEquals(res1, res2);
    }

    @Test
    void nanos2ms() {
        double nanos = 1.5e10;
        Object res1 = ClojureCaller.call("jepsen.util", "nanos->ms", nanos);
        Object res2 = Util.nanos2ms(nanos);
        Assertions.assertEquals(res1, res2);
    }

    @Test
    void nanos2secs() {
        double nanos = 1.5e10;
        Object res1 = ClojureCaller.call("jepsen.util", "nanos->secs", nanos);
        Object res2 = Util.nanos2secs(nanos);
        Assertions.assertEquals(res1, res2);
    }


    @Test
    void mapVals() {
        IFn inc = Clojure.var("clojure.core", "inc");
        Map map = Map.of(1, 2, 3, 4, 5, 7);
        Function<Object, Object> func = (k) -> (int) k + 1;

        Map res = (Map) ClojureCaller.call("jepsen.util", "map-vals", inc, map);
        Map res2 = Util.mapVals(func, map);
        Assertions.assertEquals(res.size(), res2.size());
        System.out.println(res2.get(6));
        res2.forEach((k, v) -> Assertions.assertEquals(Integer.parseInt(res.get(k).toString()), v));

    }

    @Test
    void mapKv() {
        Map map = Map.of(1, 2, 3, 4, 5, 7);
        Function<Object[], Object[]> func = (k) -> new Object[]{(int) k[0] + 1, (int) k[1] + 1};
        Map res = Util.mapKv(func, map);
        Assertions.assertFalse(res.containsKey(1));
        Assertions.assertTrue(res.containsKey(6));
        Assertions.assertEquals(res.get(6), 8);
    }

    @Test
    void longestCommonPrefix() {
        List<List<String>> test = List.of(List.of("haze", "had", "haze"), List.of("haze", "had", "haze"), List.of("haze", "had","wow"));
        LazySeq res1 = (LazySeq) ClojureCaller.call("jepsen.util", "longest-common-prefix", test);
        List<String> res2 = Util.longestCommonPrefix(test);
        Assertions.assertEquals(res1.size(), res2.size());
        for (int i = 0; i < res1.size(); i++) {
            Assertions.assertEquals(res1.get(i), res2.get(i));
        }

    }

    @Test
    void dropCommonProperPrefix() {
        List<List<String>> test = List.of(List.of("haze", "had", "haze"), List.of("haze", "had", "haze"), List.of("haze", "had","wow"));
        List<LazySeq> res1 = (List) ClojureCaller.call("jepsen.util", "drop-common-proper-prefix", test);
        List<List<String>> res2 = Util.dropCommonProperPrefix(test);
        Assertions.assertEquals(res1.size(), res2.size());
        for (int i = 0; i < res1.size(); i++) {
            List<String> s1 = res1.get(i);
            List<String> s2 = res2.get(i);
            Assertions.assertEquals(s1.size(), s2.size());
            for (String s : s1) {
                Assertions.assertTrue(s2.contains(s));
            }
        }
    }
}