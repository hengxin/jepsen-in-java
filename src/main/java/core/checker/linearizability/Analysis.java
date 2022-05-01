package core.checker.linearizability;

import core.checker.model.Inconsistent;
import core.checker.model.Model;
import core.checker.util.OpUtil;

import java.util.*;
import java.util.stream.Collectors;

public class Analysis {
    public static List<Map<String, Object>> extendPath(List<Map<String, Object>> prefix, List<Op> ops) {
        List<Map<String, Object>> path = new ArrayList<>();
        for (Map<String, Object> p : prefix) {
            path.add(new HashMap<>(p));
        }
        for (Op op : ops) {
            Model model = (Model) path.get(path.size()-1).get("model");
            Model newModel = model.step(op);
            path.add(new HashMap<>(Map.of(
                    "op", op,
                    "model", newModel
            )));
            if (newModel instanceof Inconsistent) {
                return path;
            }
        }
        return path;

    }

    public static List<List<Op>> getSubsets(List<Op> ops) {
        int n = ops.size();
        List<List<Op>> res = new ArrayList<>();

        // Run a loop from 0 to 2^n
        for (int i = 0; i < (1 << n); i++) {
            List<Op> cur = new ArrayList<>();
            int m = 1; // m is used to check set bit in binary representation.
            for (int j = 0; j < n; j++) {
                if ((i & m) > 0) {
                    cur.add(ops.get(j));
                }
                m = m << 1;
            }

            res.add(cur);
        }
        return res;
    }

    public static List<List<Op>> permutations(List<Op> ops) {
        List<List<Op>> ans = new ArrayList<>();

        // If empty
        if (ops.size() == 0) {
            ans.add(new ArrayList<>());
            return ans;
        }

        if (ops.size() == 1) {
            ans.add(ops);
            return ans;
        }


        for (int i = 0; i < ops.size(); i++) {
            Op op = ops.get(i);

            List<Op> rest = new ArrayList<>(ops.subList(0, i));
            rest.addAll(ops.subList(i + 1, ops.size()));


            List<List<Op>> recur = permutations(rest);
            for (List<Op> r : recur) {
                List<Op> cur = new ArrayList<>();
                cur.add(op);
                cur.addAll(r);
                ans.add(cur);
            }
        }
        return ans;
    }


    public static Set<List<Map<String, Object>>> finalPathsForConfig(List<Map<String, Object>> prefix, Op finalOp, List<Op> calls) {
        int finalProcess = finalOp.getProcess();
        List<Op> filtered = calls.stream().filter(
                op -> op.getProcess() != finalProcess
        ).collect(Collectors.toList());
        List<List<Op>> subsets = getSubsets(filtered);
        List<List<Op>> permutations = new ArrayList<>();
        for (List<Op> s : subsets) {
            permutations.addAll(permutations(s));
        }
        Set<List<Map<String, Object>>> res = new HashSet<>();
        if (permutations.isEmpty()) {
            permutations.add(new ArrayList<>());
        }
        for (List<Op> p : permutations) {
            p.add(finalOp);
            res.add(extendPath(prefix, p));
        }

        return res;


    }

    public static Op previousOk(List<Op> history, Op op) {
        assert OpUtil.isOk(op);
        for (int i = history.indexOf(op) - 1; i >= 0; i--) {
            op = history.get(i);
            if (OpUtil.isOk(op)) {
                return op;
            }
        }
        return null;
    }
}
