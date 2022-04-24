package core.checker.linearizability;

import core.checker.checker.Operation;
import core.checker.util.OpUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class History {
    public static void index(List<? extends Operation> history) {
        for (int i = 0; i < history.size(); i++) {
            history.get(i).setIndex(i);
        }
    }

    public static List<Integer> processes(List<? extends Operation> history) {
        return history.stream().map(Operation::getProcess).distinct().collect(Collectors.toList());
    }

    public static Op invocation(Map<Op, Op> pairIndex, Op op) {
        if (OpUtil.isInvoke(op)) {
            return op;
        } else {
            return pairIndex.getOrDefault(op,null);
        }
    }

    public static void sortProcesses(List processes) {
        processes.sort((o1, o2) -> {
            if (o1 instanceof Integer) {
                if (o2 instanceof Integer) {
                    return ((Integer) o1).compareTo((Integer) o2);
                } else return 1;
            } else if (o2 instanceof Integer) {
                return -1;
            } else {
                return (o1.toString()).compareTo(o2.toString());
            }
        });
    }

    public static List<Operation> complete(List<? extends Operation> history) {
        List<Operation> res = new ArrayList<>();
        Map<Integer, Integer> index = new HashMap<>();
        for (Operation operation : history) {
            completeFoldOp(res, index, operation);
        }
        return res;
    }

    public static Op completion(Map<Op, Op> pairIndex, Op op) {
        if (OpUtil.isInvoke(op)) {
            return pairIndex.get(op);
        } else {
            return op;
        }
    }

    public static void completeFoldOp(List<Operation> history, Map<Integer, Integer> index, Operation operation) {
        if (operation.getType() == Operation.Type.INVOKE) {
            if (index.containsKey(operation.getProcess())) {
                int prior = index.get(operation.getProcess());
                throw new RuntimeException("Process " + operation.getProcess() + " already running " + history.get(prior) + ", yet attempted to invoke " +
                        operation + " concurrently");
            }
            history.add(operation);
            index.put(operation.getProcess(), history.size() - 1);
        } else if (operation.getType() == Operation.Type.OK) {
            assert index.containsKey(operation.getProcess()) : "Processes completes an operation without a prior invocation: " + operation;
            int i = index.get(operation.getProcess());
            Operation invocation = history.get(i);
            Object value = operation.getValue();
            invocation.setValue(value);
            history.set(i, invocation);
            history.add(operation);
            index.remove(operation.getProcess());
        } else if (operation.getType() == Operation.Type.FAIL) {
            assert index.containsKey(operation.getProcess()) : "Process failed an operation without a prior invocation: " + operation;
            int i = index.get(operation.getProcess());
            Operation invocation = history.get(i);
            if (operation.getValue() == null) {
                assert invocation.getValue() == null : "invocation value " + invocation.getValue() + " and failure value " + operation.getValue() + " don't match";

            } else {
                //TODO
                //                assert operation.getValue().equals(invocation.getValue()) : "invocation value " + invocation.getValue() + " and failure value " + operation.getValue() + " don't match";
                if (!operation.getValue().equals(invocation.getValue())) {
                    operation.setValue(invocation.getValue());
                }
            }
            invocation.setValue(operation.getValue());
            invocation.setFail(true);
            history.set(i, invocation);
            history.add(operation);
            index.remove(operation.getProcess());
        } else if (operation.getType() == Operation.Type.INFO) {
            history.add(operation);
        }

    }

    public static List<Operation> crashedInvokes(List<? extends Operation> history) {
        Map<Integer, Operation> calls = new HashMap<>();
        for (Operation op : history) {
            int process = op.getProcess();
            if (OpUtil.isInvoke(op)) {
                calls.put(process, op);
            } else if (OpUtil.isOk(op)) {
                calls.remove(process);
            } else if (OpUtil.isFail(op)) {
                calls.remove(process);
            } else if (OpUtil.isInfo(op)) {

            } else {
                throw new IllegalArgumentException("Unknown op type " + op.getType() + " from " + op);
            }
        }
        return new ArrayList<>(calls.values());
    }

    public static void convertOpIndices(Map<Integer, Integer> mapping, List<Op> ops) {
        for (Op op : ops) {
            convertOpIndex(mapping, op);
        }
    }

    public static void convertOpIndex(Map<Integer, Integer> mapping, Op op) {
        if (op.getIndex() >= 0) {
            int newI = op.getIndex();
            op.setIndex(mapping.get(newI));
        }
    }

    public static Map<Op, Op> pairIndex(List<Op> history) {
        List<List<Op>> pairs = pairs(history);
        Map<Op, Op> index = new HashMap<>();
        for (List<Op> p : pairs) {
            Op invoke = p.get(0);
            assert invoke.getIndex() >= 0;
            if (p.size() > 1) {
                Op complete = p.get(1);
                index.put(complete, invoke);
                index.put(invoke, complete);
            } else {
                index.put(invoke, null);
            }
        }
        return index;

    }

    public static Map<Op, Op> pairIndexWithInvokesAndInfos(List<Op> history) {
        List<List<Op>> pairs = pairsWithInvokesAndInfos(history);
        Map<Op, Op> index = new HashMap<>();
        for (List<Op> p : pairs) {
            Op invoke = p.get(0);
            assert invoke.getIndex() >= 0;
            if (p.size() > 1) {
                Op complete = p.get(1);
                index.put(complete, invoke);
                index.put(invoke, complete);
            } else {
                index.put(invoke, null);
            }
        }
        return index;

    }


    public static List<List<Op>> pairs(List<Op> history) {
        return pairs(new HashMap<>(), history);
    }

    public static List<List<Op>> pairsWithInvokesAndInfos(List<Op> history) {
        try {
            return pairsWithInvokesAndInfos(new HashMap<>(), history);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return new ArrayList<>();

    }

    public static List<List<Op>> pairs(Map<Integer, Op> invocations, List<Op> ops) {
        List<List<Op>> res = new ArrayList<>();
        for (Op op : ops) {
            if (op.getType() == Operation.Type.INFO) {
                res.add(new ArrayList<>(List.of(op)));
            } else if (op.getType() == Operation.Type.INVOKE) {
                assert !invocations.containsKey(op.getProcess());
                invocations.put(op.getProcess(), op);
            } else if (op.getType() == Operation.Type.OK || op.getType() == Operation.Type.FAIL) {
                assert invocations.containsKey(op.getProcess());
                res.add(new ArrayList<>(List.of(invocations.get(op.getProcess()), op)));
                invocations.remove(op.getProcess());
            }
        }

        return res;
    }

    public static List<List<Op>> pairsWithInvokesAndInfos(Map<Integer, Op> invocations, List<Op> ops) throws Exception {
        List<List<Op>> res = new ArrayList<>();
        for (Op op : ops) {
            int p = op.getProcess();
            Operation.Type type = op.getType();
            if (type == Operation.Type.INVOKE) {
                if (invocations.containsKey(p)) {
                    throw new Exception("Process " + p + " is still executing " + invocations.get(p) + " cannot invoke");
                }
                invocations.put(p, op);
            } else if (type == Operation.Type.INFO) {
                if (invocations.containsKey(p)) {
                    res.add(new ArrayList<>(List.of(invocations.get(p), op)));
                    invocations.remove(p);
                } else {
                    res.add(new ArrayList<>(List.of(op)));
                }
            } else if (type == Operation.Type.OK || type == Operation.Type.FAIL) {
                if (!invocations.containsKey(p)) {
                    throw new Exception("Process " + p + " can not complete " + op + " without invoking it first");
                }
                res.add(new ArrayList<>(List.of(invocations.get(p), op)));
                invocations.remove(p);
            }
        }
        return res;
    }

    public static boolean isIndexed(List<Op> history) {
        return history.isEmpty() || history.get(0).getIndex() >= 0;
    }

    public static void ensureIndex(List<Op> history) {
        if (!isIndexed(history)) {
            index(history);
        }
    }

    public static void withoutFailures(List<Op> history) {
        history.removeIf(op -> OpUtil.isFail(op) || op.isFail());
    }

    public static void withSyntheticInfos(List<Op> history) {
        assert !history.isEmpty();
        int maxIndex = history.stream().map(Op::getIndex).reduce(0, Math::max);
        List<Op> unmatched = unmatchedInvokes(history);
        for (int i = 0; i < unmatched.size(); i++) {
            Op invoke = new Op(unmatched.get(i));
            invoke.setType(Operation.Type.INFO);
            invoke.setIndex((i + 1) + maxIndex);
            history.add(invoke);
        }
    }

    public static List<Op> unmatchedInvokes(List<Op> history) {
        Map<Integer, Op> calls = new HashMap<>();
        for (Op op : history) {
            int process = op.getProcess();
            if (OpUtil.isInvoke(op)) {
                calls.put(process, op);
            } else if (OpUtil.isOk(op)) {
                calls.remove(process);
            } else if (OpUtil.isFail(op)) {
                calls.remove(process);
            } else if (OpUtil.isInfo(op)) {
                calls.remove(process);
            } else {
                throw new IllegalArgumentException("Unknown op type " + op.getType() + " from " + op);
            }
        }
        return new ArrayList<>(calls.values());
    }

    public static Map<Integer, Integer> internalIndex(List<Op> history) {
        List<Integer> indices = history.stream().map(Op::getIndex).collect(Collectors.toList());
        if (indices.stream().allMatch(i -> i == -1) || indices.stream().distinct().count() == indices.size()) {
            List<Op> newHis = new ArrayList<>(history);
            index(newHis);
            Map<Integer, Integer> m = new HashMap<>();
            for (int i = 0; i < newHis.size(); i++) {
                m.put(newHis.get(i).getIndex(), indices.get(i));
            }
            return m;
        } else {
            throw new IllegalArgumentException("History starting with " + history.get(0) + " contains ops with non-unique indices");
        }

    }


    public static List<Object> preprocess(List<Op> history) {
        ensureIndex(history);
        history = complete(history).stream().map(operation -> (Op) operation).collect(Collectors.toList());
        withoutFailures(history);
        withSyntheticInfos(history);
        Map<Integer, Integer> internalToExternal = internalIndex(history);
        return new ArrayList<>(List.of(history, internalToExternal));
    }
}
