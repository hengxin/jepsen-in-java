package tests.bank;

import core.checker.checker.Checker;
import core.checker.checker.Operation;
import core.checker.util.OpUtil;
import core.checker.vo.Result;

import java.util.*;
import java.util.stream.Collectors;

class BankChecker implements Checker {
    private Map<String, Object> checkerOpts;

    public BankChecker(Map<String, Object> checkerOpts) {
        this.checkerOpts = checkerOpts;
    }

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        Set<?> accts = (Set<?>) test.get("accounts");
        double total = (double) test.get("total-amount");
        List<Operation> reads = history.stream().filter(OpUtil::isOk).filter(op -> op.getF() == Operation.F.READ).collect(Collectors.toList());
        Map<String, List<Map<String, Object>>> errors = reads.stream().map(op -> Bank.checkOp(accts, total, (boolean) checkerOpts.get("negative-balances?"), op)).collect(Collectors.groupingBy(m -> (String) m.get("type")));
        List<Map<String, Object>> firstErrors = errors.values().stream().map(v -> {
            if (v.size() > 0) {
                return v.get(0);
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        Operation firstError = null;
        if (firstErrors.size() > 0) {
            firstError = (Operation) firstErrors.get(0).get("op");
            for (Map<String, Object> error : firstErrors.subList(1, firstErrors.size())) {
                if (((Operation) error.get("op")).getIndex() < firstError.getIndex()) {
                    firstError = (Operation) error.get("op");
                }
            }
        }

        List<Map<String, Object>> error = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : errors.entrySet()) {
            String type = entry.getKey();
            List<Map<String, Object>> errs = entry.getValue();
            Map<String, Object> worst = null;
            if (errs.size() > 0) {
                worst = errs.get(0);
                for (Map<String, Object> e : errs) {
                    if (Bank.errBadness(test, e) > Bank.errBadness(test, worst)) {
                        worst = e;
                    }
                }
            }
            Map<String, Object> e = new HashMap<>(Map.of(
                    "count", errs.size(),
                    "first", errs.get(0),
                    "worst", worst,
                    "last", errs.get(errs.size() - 1)
            ));
            if (type.equals("wrong-total")) {
                double lowest = Double.MAX_VALUE;
                for (Map<String, Object> m : errs) {
                    if ((double) m.get("total") < lowest) {
                        lowest = (double) m.get("total");
                    }
                }
                double highest = Double.MIN_VALUE;
                for (Map<String, Object> m : errs) {
                    if ((double) m.get("total") > highest) {
                        highest = (double) m.get("total");
                    }
                }
                e.put("lowest", lowest);
                e.put("highest", highest);
                error.add(e);
            }
        }

        boolean valid = errors.values().stream().allMatch(List::isEmpty);
        Map<String, Object> res = new HashMap<>(Map.of(
                "valid?", valid,
                "read-count", reads.size(),
                "error-count", errors.values().stream().map(List::size).reduce(Integer::sum),
                "first-error", firstError,
                "errors", error
        ));
        Result result = new Result(valid);

        result.setRes(res);
        return result;

    }
}
