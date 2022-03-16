package util;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class Util {
    /**
     * @param set integers
     * @return sorted, compact string representation
     */
    public static String integerIntervalSetStr(Set<Integer> set) {

        List<Integer> sorted = set.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        List<List<Integer>> runs = new ArrayList<>();
        int start = Integer.MAX_VALUE, end = Integer.MAX_VALUE;
        for (Integer cur : sorted) {
            if (start == Integer.MAX_VALUE) {
                start = cur;
                end = cur;
            } else if (end + 1 == cur) {
                end = cur;
            } else {
                runs.add(List.of(start, end));
                start = cur;
                end = cur;
            }
        }

        if (start != Integer.MAX_VALUE) {
            runs.add(List.of(start, end));
        }

        StringBuilder res = new StringBuilder("#{");
        List<String> numbers = new ArrayList<>();
        for (List<Integer> pair : runs) {
            start = pair.get(0);
            end = pair.get(1);
            if (start == end) {
                numbers.add(String.valueOf(start));
            } else {
                numbers.add(start + ".." + end);
            }
        }
        res.append(String.join(" ", numbers));
        res.append("}");
        return res.toString();
    }

    /**
     * @param nanos nanosecond
     * @return millisecond
     */
    public static long nanos2ms(long nanos) {
        return nanos / 1000000;
    }

    /**
     * @param function param:[k,v] return:[k,v]
     * @param m map
     * @return new map
     */
    public static Map mapKv(Function<Object[],Object[]> function, Map m) {
        Map res = new HashMap();
        try {
            m.forEach((k, v) -> {
                Object[] kv = function.apply(new Object[]{k, v});
                res.put(kv[0], kv[1]);
            });
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return res;
    }

    public static void main(String[] args) {
        IFn require = Clojure.var("clojure.core", "inc");
        Map<Integer,Integer> map = Map.of(1, 2, 3, 4, 5, 7);
        Function<Object[], Object[]> func = (k) -> new Object[]{(int)k[0] + 1, k[1]};

        String res = ClojureCaller.call("jepsen.util", "map-keys", require, map).toString();
        String res2 = mapKv(func, map).toString();
        //        Object[] res=Map.of(1,2,3,4).entrySet().toArray();
        log.info("res1: " + res);
        log.info("res2: " + res2);
        //        log.info("conclusion: "+res.toString().equals());
    }
}
