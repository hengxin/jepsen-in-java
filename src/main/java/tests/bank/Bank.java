package tests.bank;

import core.checker.checker.Compose;
import core.checker.checker.Operation;
import core.checker.util.OpUtil;
import lombok.extern.slf4j.Slf4j;
import tests.Invoke;
import util.Util;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Bank {

    public static Invoke read() {
        return new Invoke(Operation.Type.INVOKE, Operation.F.READ, null);
    }

    public static Invoke transfer(Map<String, Object> test) {
        List<?> accounts = (List<?>) test.get("accounts");
        int maxTransfer = (int) test.get("max-transfer");
        Random random = new Random();
        Map<String, Object> value = new HashMap<>(Map.of(
                "from", accounts.get(random.nextInt(accounts.size())),
                "to", accounts.get(random.nextInt(accounts.size())),
                "amount", random.nextInt(maxTransfer) + 1
        ));
        return new Invoke(Operation.Type.INVOKE, Operation.F.TRANSFER, value);
    }

    public static List<Invoke> diffTransfer(List<Invoke> ops) {
        return ops.stream().filter(op -> {
            Map<?, ?> value = (Map<?, ?>) op.getValue();
            return !value.get("from").equals(value.get("to"));
        }).collect(Collectors.toList());
    }

    public static Double errBadness(Map<String, Object> test, Map<String, Object> err) {
        switch ((String) err.get("type")) {
            case "unexpected-key":
                return (double) ((List<?>) err.get("unexpected")).size();
            case "nil-balance":
                return (double) ((List<?>) err.get("nil")).size();
            case "wrong-total":
                return (double) Math.abs((float) (((double) err.get("total") - (double) test.get("total-amount")) / (double) test.get("total-amount")));
            case "negative-value":
                return ((List<Double>) err.get("negative")).stream().reduce(Double::sum).get();
        }
        return null;
    }

    public static Map<String, Object> checkOp(Set<?> accts, double total, boolean negativeBalances, Operation op) {
        List<?> ks = Arrays.asList(((Map<?, ?>) op.getValue()).keySet().toArray());
        List<Double> balances = new ArrayList<>(((Map<?, Double>) op.getValue()).values());
        if (!accts.containsAll(ks)) {
            return new HashMap<>(Map.of(
                    "type", "unexpected-key",
                    "unexpected", ks.removeAll(accts),
                    "op", op
            ));
        } else if (balances.stream().anyMatch(Objects::isNull)) {
            Map<?, ?> value = (Map<?, ?>) op.getValue();
            for (Object v : value.values()) {
                value.remove(v);
            }
            return new HashMap<>(Map.of(
                    "type", "il-balance",
                    "nils", value,
                    "op", op
            ));
        } else if (balances.stream().reduce(Double::sum).get() != total) {
            return new HashMap<>(Map.of(
                    "type", "wrong-total",
                    "total", balances.stream().reduce(Double::sum).get(),
                    "op", op
            ));
        } else if (!negativeBalances && balances.stream().anyMatch(b -> b < 0)) {
            return new HashMap<>(Map.of(
                    "type", "negative-value",
                    "negative", balances.stream().filter(b -> b < 0),
                    "op", op
            ));
        }
        return null;
    }


    public static List<Operation> okReads(List<Operation> history) {
        List<Operation> h = history.stream().filter(op -> OpUtil.isOk(op) && op.getF() == Operation.F.READ).collect(Collectors.toList());
        return h;
    }

    public static Map<?, List<Operation>> byNode(Map<String, Object> test, List<Operation> history) {
        List<?> nodes = (List<?>) test.get("nodes");
        int n = nodes.size();

        return history.stream().filter(h -> h.getProcess() >= 0).collect(Collectors.groupingBy(op -> {
            int p = op.getProcess();
            return nodes.get(p % n);
        }));
    }

    public static List<List<Double>> points(List<Operation> history) {
        List<List<Double>> res = new ArrayList<>();
        for (Operation op : history) {
            double time = Util.nanos2secs(op.getTime());
            double totalOfAccounts = ((Map<?, Double>) op.getValue()).values().stream().filter(Objects::nonNull).reduce(Double::sum).get();
            res.add(new ArrayList<>(List.of(time, totalOfAccounts)));
        }
        return res;
    }


    public static Map<String, Object> test() {
        return test(new HashMap<>(Map.of(
                "negative-balances?", false
        )));
    }

    public static Map<String, Object> test(Map<String, Object> opts) {
        return new HashMap<>(Map.of(
                "max-transfer", 5,
                "total-amount", 100,
                "accounts", new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7)),
                "checker", new Compose(new HashMap<>(Map.of(
                        "SI", new BankChecker(opts),
                        "plot", new BankPlotter()

                )))
        ));
    }
}
