package tests.long_fork;

import core.checker.checker.Operation;
import core.checker.exception.IllegalHistory;
import lombok.extern.slf4j.Slf4j;
import util.Util;

import java.util.*;
import java.util.stream.Collectors;

//TODO jepsen txn
@Slf4j
public class LongFork {
    static Random random = new Random();

    public static List<Integer> groupFor(int n, int k) {
        int m = k % n;
        int l = k - m;
        int u = l + n;
        List<Integer> res = new ArrayList<>();
        for (int i = l; i < u; i++) {
            res.add(i);
        }
        return res;
    }

    public static List<List<?>> readTxnFor(int n, int k) {
        groupFor(n, k);
        List<List<?>> permutations = Util.permutations(groupFor(n, k));
        List<?> shuffle = permutations.get(random.nextInt(permutations.size()));
        List<List<?>> res = new ArrayList<>();
        for (Object s : shuffle) {
            res.add(List.of("r", s, Optional.empty()));
        }
        return res;
    }

    public static Object readCompare(Map<?, ?> a, Map<?, Object> b) throws IllegalHistory {
        if (a.size() != b.size()) {
            throw new IllegalHistory(List.of(a, b), "These read did not query for the same keys, and therefore cannot be compared");

        }
        int res = 0;
        for (Object k : a.keySet()) {
            Object va = a.get(k);
            Object vb = b.getOrDefault(k, "not-found");
            if (vb.equals("not-found")) {
                throw new IllegalHistory(List.of(a, b), k, "These reads did not query for the same keys, and therefore cannot be compared");
            }

            if (va.equals(vb)) {
                continue;
            }

            if (vb == null) {
                if (res > 0) {
                    return null;
                } else {
                    res = -1;
                }
            }

            if (va == null) {
                if (res < 0) {
                    return null;
                } else {
                    res = 1;
                }
            }

            throw new IllegalHistory(List.of(a, b), k, "These two read states contain distinct values for the same key; this checker assumes only one write occurs per key.");
        }
        return res;

    }

    public static List<List<?>> distinctPairs(List<?> coll) {
        List<Set<?>> distinctPairs = new ArrayList<>();
        for (Object a : coll) {
            for (Object b : coll) {
                if (!a.equals(b)) {
                    distinctPairs.add(new HashSet<>(Set.of(a, b)));
                }
            }
        }
        return distinctPairs.stream().distinct().map(ArrayList::new).collect(Collectors.toList());
    }

    public static Map<?, Object> readOp2ValueMap(Operation op) {
        List<?> ops = (List<?>) op.getValue();
        Map<Object, Object> m = new HashMap<>();
        for (Object o : ops) {
            List<?> v = (List<?>) o;
            m.put(v.get(1), v.get(2));
        }
        return m;
    }

    public static List<List<?>> findForks(List<Operation> ops) {
        List<List<?>> distinctPairs = distinctPairs(ops);
        List<List<?>> res = new ArrayList<>();
        for (List<?> pair : distinctPairs) {
            Operation a = (Operation) pair.get(0);
            Operation b = (Operation) pair.get(1);
            try {
                if (readCompare(readOp2ValueMap(a), readOp2ValueMap(b)) == null) {
                    res.add(List.of(a, b));
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }

        }
        return res;

    }

    //    public static void isReadTxn()
}
