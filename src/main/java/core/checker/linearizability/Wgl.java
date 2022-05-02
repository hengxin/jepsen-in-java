package core.checker.linearizability;

import core.checker.checker.Operation;
import core.checker.model.Inconsistent;
import core.checker.model.Model;
import core.checker.util.OpUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class Wgl {

    public static BitSet linearized(CacheConfig c) {
        return c.getLinearized();
    }

    public CacheConfig cacheConfig(BitSet linearized, Model model, Op op) {
        return new CacheConfig(linearized, model, op);
    }


    /**
     * assign to invocation and its corresponding completion a same entry id
     * ok invocations from 0 to c and crashed operations from c+1 to n
     *
     * @param history history
     * @return newHis
     */
    public static List<Op> withEntryIds(List<Op> history) {
        List<Op> newHis = new ArrayList<>();
        // call process corresponds to op index in newHis
        Map<Integer, Integer> calls = new HashMap<>();
        int i = 0;
        int eOk = 0;// current entry id
        int eInfo = history.size() - History.crashedInvokes(history).size();
        while (history.size() > i) {
            Op op = history.get(i);
            int p = op.getProcess();
            if (OpUtil.isInvoke(op)) {
                newHis.add(op);
                calls.put(p, i);
                i++;
            } else if (OpUtil.isOk(op)) {
                int invokeI = calls.get(p);
                Op invoke = newHis.get(invokeI);
                invoke.setEntryId(eOk);
                newHis.set(invokeI, invoke);
                op.setEntryId(eOk);
                newHis.add(op);
                calls.remove(p);
                i++;
                eOk++;
            } else if (OpUtil.isFail(op)) {
                assert false : "Err, shouldn't have a failure here: " + op;
            } else if (OpUtil.isInfo(op)) {
                //if info corresponds to an earlier invoke
                if (calls.containsKey(p)) {
                    int invokeI = calls.get(p);
                    Op invoke = newHis.get(invokeI);
                    invoke.setEntryId(eInfo);
                    newHis.set(invokeI, invoke);
                    op.setEntryId(eInfo);
                    newHis.add(op);
                    calls.remove(p);
                    i++;
                    eInfo++;
                }
                //random info
                else {
                    newHis.add(op);
                    i++;
                }
            }
        }
        assert calls.size() == 0;
        return newHis;
    }

    public static int maxEntryId(List<Op> history) {
        int max = -1;
        for (Op op : history) {
            max = Math.max(op.getEntryId(), max);
        }
        return max;
    }

    public static int bitsetHighestContiguousLinearized(BitSet b) {
        return b.nextClearBit(0) - 1;
    }

    public static List<CacheConfig> configurationsWhichLinearizedUpTo(int entryId, List<CacheConfig> configs) {
        return configs.stream().filter(config -> entryId <= bitsetHighestContiguousLinearized(config.getLinearized())).collect(Collectors.toList());
    }

    public static int highestContiguousLinearizedEntryId(List<CacheConfig> configs) {
        return configs.stream().map(CacheConfig::getLinearized).map(Wgl::bitsetHighestContiguousLinearized).reduce(-1, Math::max);
    }

    public static List<Op> invokeAndOkForEntryId(List<Op> history, int entryId) {
        List<Op> found = null;
        Op invoke = null;
        for (Op op : history) {
            if (invoke != null) {
                // find completion
                if (invoke.getProcess() == op.getProcess()) {
                    found = new ArrayList<>(List.of(invoke, op));
                    break;
                }
            } else {
                // find invoke
                if (entryId == op.getEntryId()) {
                    invoke = op;
                }
            }
        }

        assert found != null;
        return found;

    }

    public static List<Op> historyWithoutLinearized(List<Op> history, BitSet linearized) {
        List<Op> newHis = new ArrayList<>();
        Set<Integer> calls = new HashSet<>();
        for (Op op : history) {
            int process = op.getProcess();
            int entryId = op.getEntryId();
            if (entryId >= 0 && linearized.get(entryId)) {
                calls.add(process);
            } else if (calls.contains(process)) {
                calls.remove(process);
            } else {
                newHis.add(op);
            }
        }
        return newHis;
    }

    public static List<Op> concurrentOpsAt(List<Op> history, Op targetOp) {
        Map<Integer, Op> concurrent = new HashMap<>();
        for (Op op : history) {
            int process = op.getProcess();
            if (op == targetOp) {
                return new ArrayList<>(concurrent.values());
            } else if (OpUtil.isInvoke(op)) {
                concurrent.put(process, op);
            } else if (OpUtil.isInfo(op)) {

            } else {
                concurrent.remove(process);
            }
        }
        return new ArrayList<>(concurrent.values());
    }

    //    public static void renderOp()
    public static Set<List<Map<String, Object>>> finalPaths(List<Op> history, Map<Op, Op> pairIndex, Op finalOp, List<CacheConfig> configs, Map<Integer, Integer> indices) {
        List<Op> calls = concurrentOpsAt(history, finalOp);
        Set<List<Map<String, Object>>> paths = new HashSet<>();
        for (CacheConfig config : configs) {
            BitSet linearized = config.getLinearized();
            if (linearized.isEmpty()) continue;
            Model model = config.getModel();
            List<Op> curCall=new ArrayList<>(calls);
            curCall.removeIf(call -> linearized.get(call.getEntryId()));
            curCall = curCall.stream().map(pairIndex::get).collect(Collectors.toList());
            Map<String, Object> p = new HashMap<>();
            p.put("op", pairIndex.get(config.getOperation()));
            p.put("model", model);
            List<Map<String, Object>> path = new ArrayList<>(List.of(p));
            Set<List<Map<String, Object>>> finalPaths = Analysis.finalPathsForConfig(path, finalOp, curCall);
            for (List<Map<String, Object>> list : finalPaths) {
                for (Map<String, Object> m : list) {
                    m.put("op", getOriginalOp(indices, (Op) m.get("op")));
                }
            }
            paths.addAll(finalPaths);
        }
        return new HashSet<>(paths);
    }

    public static Op getOriginalOp(Map<Integer, Integer> indices, Op op) {
        if (op == null) return null;
        Op res = new Op(op);
        if (op.getIndex() >= 0) {
            int newI = op.getIndex();
            res.setIndex(indices.get(newI));
        }
        return res;
    }

    public static Map<String, Object> invalidAnalysis(List<Op> history, List<CacheConfig> configs, Map<String, Object> state) {
        Map<Op, Op> pairIndex = History.pairIndexWithInvokesAndInfos(history);
        int finalEntryId = highestContiguousLinearizedEntryId(configs) + 1;
        List<Op> invokeAndOk = invokeAndOkForEntryId(history, finalEntryId);
        Op finalInvoke = invokeAndOk.get(0);
        Op finalOk = invokeAndOk.get(1);
        Op previousOk = Analysis.previousOk(history, finalOk);
        configs = configurationsWhichLinearizedUpTo(finalEntryId - 1, configs);
        Map<Integer, Integer> indices = (Map<Integer, Integer>) state.get("indices");
        Set<List<Map<String, Object>>> finalPaths = finalPaths(history, pairIndex, finalOk, configs, indices);
        finalOk = getOriginalOp(indices, finalOk);
        previousOk = getOriginalOp(indices, previousOk);
        HashMap<String, Object> res = new HashMap<>(Map.of(
                "valid?", false,
                "final-paths", finalPaths
        ));
        res.put("op", finalOk);
        res.put("previous-ok", previousOk);
        return res;
    }

    public static WglNode ddlHistory(List<Op> history) {
        WglNode head = new WglNode(null, null, null, null);
        Map<Integer, WglNode> calls = new HashMap<>();
        List<Op> infos = new ArrayList<>();
        WglNode prev = head;
        for (Op op : history) {
            if (OpUtil.isInvoke(op)) {
                WglNode node = new WglNode(prev, null, op, null);
                prev.setNext(node);
                calls.put(op.getProcess(), node);
                prev = node;
            } else if (OpUtil.isOk(op)) {
                WglNode node = new WglNode(prev, null, op, null);
                WglNode invoke = calls.get(op.getProcess());
                prev.setNext(node);
                invoke.setMatch(node);
                calls.remove(op.getProcess());
                prev = node;
            } else if (OpUtil.isFail(op)) {
                throw new IllegalArgumentException("Can't compute ddl-histories with :fail ops");
            } else if (OpUtil.isInfo(op)) {
                infos.add(op);
            }
        }
        while (!infos.isEmpty()) {
            for (Op op : infos) {
                if (calls.containsKey(op.getProcess())) {
                    WglNode invoke = calls.get(op.getProcess());
                    WglNode node = new WglNode(prev, null, op, null);
                    prev.setNext(node);
                    invoke.setMatch(node);
                    calls.remove(op.getProcess());
                    prev = node;
                }
            }
            infos.clear();

            if (!calls.isEmpty()) {
                log.debug("Expected all invocations to have a matching :ok or :info, but invocations by process " + calls.keySet() + " went unmatched. This might indicate a malformed history");
            }

            infos = calls.entrySet().stream().map(e -> {
                int process = e.getKey();
                WglNode invokeNode = e.getValue();
                Op op = invokeNode.getOp();
                return new Op(process, op.getF(), core.checker.checker.Operation.Type.INFO, op.getValue());
            }).collect(Collectors.toList());
        }
        return head;
    }

    public static void lift(WglNode entry) {
        WglNode prev = entry.getPrev();
        WglNode next = entry.getNext();
        prev.setNext(next);
        next.setPrev(prev);
        WglNode match = entry.getMatch();
        if (match != null) {
            match.getPrev().setNext(match.getNext());
            WglNode n = match.getNext();
            if (n != null) {
                n.setPrev(match.getPrev());
            }
        }

    }

    public static void unlift(WglNode entry) {
        WglNode match = entry.getMatch();
        if (match != null) {
            match.getPrev().setNext(match);
            WglNode n = match.getNext();
            if (n != null) {
                n.setPrev(match);
            }
        }
        entry.getPrev().setNext(entry);
        entry.getNext().setPrev(entry);
    }

    public static Map<String, Object> check(Model model, List<Op> history, Map<String, Object> state) {
        history=history.stream().map(Op::new).collect(Collectors.toList());
        List<Object> historyAndIndex = History.preprocess(history);
        history = (List<Op>) historyAndIndex.get(0);
        Map<Integer, Integer> internalToExternal = (Map<Integer, Integer>) historyAndIndex.get(1);
        state.put("indices", internalToExternal);
        history = withEntryIds(history);
        int n = Math.max(maxEntryId(history), 0);
        WglNode headEntry = ddlHistory(history);
        BitSet linearized = new BitSet(n);
        HashSet<CacheConfig> cache = new HashSet<>();
        cache.add(new CacheConfig(new BitSet(), model, null));
        ArrayDeque<List<Object>> calls = new ArrayDeque<>();
        Model s = model;
        WglNode entry = headEntry.getNext();

        while (true) {
            if (state.getOrDefault("running?", false).equals(false)) {
                return new HashMap<>(Map.of(
                        "valid?", "unknown",
                        "cause", state.get("cause")
                ));
            } else if (headEntry.getNext() == null) {
                return new HashMap<>(Map.of(
                        "valid?", true,
                        "model", Memo.unwrap(s)
                ));
            } else {
                Op op = entry.getOp();
                core.checker.checker.Operation.Type type = op.getType();
                if (type == core.checker.checker.Operation.Type.INVOKE) {
                    Model newS = s.step(op);
                    if (newS instanceof Inconsistent) {
                        entry = entry.getNext();
                    } else {
                        int entryId = op.getEntryId();
                        BitSet newLinearized = (BitSet) linearized.clone();
                        newLinearized.set(entryId);
                        boolean newConfig = cache.add(new CacheConfig(newLinearized, newS, op));
                        if (!newConfig) {
                            entry = entry.getNext();
                        } else {
                            calls.addFirst(new ArrayList<>(List.of(entry, s)));
                            s = newS;
                            linearized.set(entryId);
                            lift(entry);
                            entry = headEntry.getNext();
                        }
                    }
                } else if (type == Operation.Type.OK) {
                    if (calls.isEmpty()) {
                        return invalidAnalysis(history, new ArrayList<>(cache), state);
                    } else {
                        List<Object> es = calls.removeFirst();
                        entry = (WglNode) es.get(0);
                        s = (Model) es.get(1);
                        op = entry.getOp();
                        linearized.set(op.getEntryId(), false);
                        unlift(entry);
                        entry = entry.getNext();
                    }
                } else if (type == Operation.Type.INFO) {
                    return new HashMap<>(Map.of(
                            "valid?", true,
                            "model", Memo.unwrap(s)
                    ));
                }
            }
        }
    }

    public static Map<String, Object> startAnalysis(Model model, List<Op> history) {
        Map<String, Object> state = new HashMap<>(Map.of(
                "running?", true
        ));

        Map<String, Object> result;
        Callable<Map<String, Object>> task = () -> check(model, history, state);

        Future<Map<String, Object>> future = Executors.newCachedThreadPool().submit(task);
        try {
            result = future.get();
        } catch (InterruptedException e) {
            Object cause = state.get("cause");
            result = new HashMap<>(Map.of(
                    "valid?", "unknown",
                    "cause", cause
            ));
        } catch (Exception e) {
            result = new HashMap<>(Map.of(
                    "exception", e
            ));
        }

        return result;

    }

    public static Map<String, Object> analysis(Model model, List<Op> history) {
        return analysis(model, history, new HashMap<>());
    }

    public static Map<String, Object> analysis(Model model, List<Op> history, Map<String, Object> opts) {
        Map<String, Object> result = startAnalysis(model, history);
        result.put("analyzer", "wgl");
        return result;
    }
}
