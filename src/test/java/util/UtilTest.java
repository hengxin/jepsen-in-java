package util;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

class UtilTest {

    @Test
    void integerIntervalSetStr() {
        Set<Integer> test=Set.of(1, 0, 3, 10);
        String res1 = ClojureCaller.call("jepsen.util", "integer-interval-set-str", test).toString();
        String res2=Util.integerIntervalSetStr(test);
        Assertions.assertEquals(res1,res2);
    }

    @Test
    void integerIntervalSetStr2() {
        Set<Integer> test=Set.of();
        String res1 = ClojureCaller.call("jepsen.util", "integer-interval-set-str", test).toString();
        String res2=Util.integerIntervalSetStr(test);
        Assertions.assertEquals(res1,res2);
    }


    @Test
    void mapKv() {
        IFn inc = Clojure.var("clojure.core", "inc");
        Map map = Map.of(1, 2, 3, 4, 5, 7);
        Function<Object[], Object[]> func = (k) -> new Object[]{(int)k[0] + 1, k[1]};

        Map res = (Map)ClojureCaller.call("jepsen.util", "map-keys", inc, map);
        Map res2 = Util.mapKv(func, map);
        Assertions.assertEquals(res.size(),res2.size());
        System.out.println(res2.get(6));
        res2.forEach((k,v)-> Assertions.assertEquals(res.get(k),v));
    }
}