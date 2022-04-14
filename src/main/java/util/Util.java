package util;

import com.google.common.collect.Lists;
import core.checker.checker.Operation;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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
    public static double nanos2ms(double nanos) {
        return nanos / 1000000;
    }

    public static double nanos2secs(double nanos) {
        return (nanos / 1e9);
    }

    /**
     * @param function param:[k,v] return:[k,v]
     * @param m        map
     * @return new map
     */
    public static Map mapKv(Function<Object[], Object[]> function, Map m) {
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

    public static Map mapVals(Function<Object, Object> function, Map m) {
        Map res = new HashMap();
        try {
            m.forEach((k, v) -> {
                v = function.apply(v);
                res.put(k, v);
            });
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return res;

    }

    public static List<String> longestCommonPrefix(List<List<String>> cs) {
        if (cs == null || cs.isEmpty()) {
            return new ArrayList<>();
        }
        if (cs.size() == 1) {
            return cs.get(0);
        }

        return cs.stream().reduce((s1, s2) -> {
                    int size = Math.min(s1.size(), s2.size());
                    int len;
                    for (len = 0; len < size; len++) {
                        if (!s1.get(len).equals(s2.get(len))) {
                            break;
                        }
                    }
                    if (len == s1.size()) return s1;
                    else return s2.subList(0, len);

                }
        ).get();

    }

    public static List<List<String>> dropCommonProperPrefix(List<List<String>> cs) {
        int min = longestCommonPrefix(cs).size();
        for (List<String> s : cs) {
            if (s.size() - 1 < min) {
                min = s.size() - 1;
            }
        }
        List<List<String>> res = new ArrayList<>();
        for (List<String> s : cs) {
            res.add(s.subList(min, s.size()));
        }
        return res;
    }

    public static void nemesisIntervals(List<Map> history) {
        nemesisIntervals(history, Map.of());
    }

    public static List nemesisIntervals(List<Map> history, Map opts) {
        //TODO set?
        List start = (List) opts.get("start");
        List stop = (List) opts.get("stop");
        List tmp = history.stream().filter(
                h -> h.get("process").equals("nemesis")
        ).collect(Collectors.toList());
        List<List<Map>> partition = Lists.partition(tmp, 2);
        List<List<Map>> filter = partition.stream().filter(
                p -> {
                    Map a = p.get(0);
                    Map b = p.get(1);
                    return a.get("f").equals(b.get("f"));
                }
        ).collect(Collectors.toList());

        List intervals = new ArrayList();
        List<List> starts = new ArrayList();
        for (List<Map> fil : filter) {
            Map a = fil.get(0);
            Map b = fil.get(1);
            Object f = a.get("f");
            if (start.contains(f)) {
                starts.add(fil);
            } else if (stop.contains(f)) {
                for (List s : starts) {
                    Object s1 = s.get(0);
                    Object s2 = s.get(1);
                    intervals.add(List.of(s1, a));
                    intervals.add(List.of(s2, b));
                }
                starts = new ArrayList<>();
            }
        }
        for (List s : starts) {
            Object s1 = s.get(0);
            Object s2 = s.get(1);
            intervals.add(List.of(s1, "null"));
            intervals.add(List.of(s2, "null"));
        }
        return intervals;
    }

    public static List<Operation> history2Latencies(List<Operation> history) {
        Map<Operation, Integer> idx = new HashMap<>();
        int i = 0;
        for (Operation operation : history) {
            idx.put(operation, i);
            i++;
        }

        LinkedHashMap<Integer, Integer> invokes = new LinkedHashMap<>();
        List<Operation> newHis = new ArrayList<>();
        for (Operation operation : history) {
            if (operation.getType() == Operation.Type.INVOKE) {
                newHis.add(operation);
                invokes.put(operation.getProcess(), newHis.size() - 1);

            } else {
                if (invokes.containsKey(operation.getProcess())) {
                    int invokeIdx = invokes.get(operation.getProcess());
                    Operation invoke = newHis.get(invokeIdx);
                    double l = operation.getTime() - invoke.getTime();
                    operation.setLatency(l);
                    invoke.setLatency(l);
                    invoke.setCompletion(operation);
                    newHis.set(invokeIdx, invoke);
                    newHis.add(operation);
                    invokes.remove(operation.getProcess());
                } else {
                    newHis.add(operation);
                }
            }
        }
        return newHis;
    }


    public static void main(String[] args) {
        //        log.info("res1: " <"re");
        //        log.info("res2: " + res2);
    }
}
