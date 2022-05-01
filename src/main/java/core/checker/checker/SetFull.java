package core.checker.checker;

import core.checker.linearizability.Op;
import core.checker.vo.Result;
import util.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A more rigorous set analysis. We allow :add operations which add a single
 *   element, and :reads which return all elements present at that time.
 *
 */
public class SetFull implements Checker {
    private final Map<String, Object> checkerOpts;

    public SetFull(Map checkerOpts) {
        this.checkerOpts = checkerOpts;
    }

    public SetFull() {
        this.checkerOpts = new HashMap<>(Map.of(
                "linearizable?", false
        ));
    }

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        history = history.stream().filter(h -> h.getProcess() >= 0).collect(Collectors.toList());
        Map<Object, SetFullElement> elements = new HashMap<>();
        Map reads = new HashMap<>(), dups = new HashMap<>();
        for (Operation operation : history) {
            List<Map> res = red(elements, reads, dups, operation);
            elements = res.get(0);
            reads = res.get(1);
            dups = res.get(2);
        }
        elements = new TreeMap<>(elements);

        Map<String, Object> setResults = CheckerUtil.setFullResults(checkerOpts, new ArrayList<>(elements.values()));
        Object valid;
        if(!dups.isEmpty()){
            valid=false;
        }else {
            valid=setResults.get("valid?");
        }
        setResults.put("valid?", valid);
        setResults.put("duplicated-count", dups.size());
        setResults.put("duplicated", dups);
        Result result = new Result();
        result.setValid(valid);
        result.setRes(setResults);
        return result;
    }

    private List<Map> red(Map<Object, SetFullElement> elements, Map<Integer, Operation> reads, Map<?, ?> dups, Operation operation) {
        Object v = operation.getValue();
        int p = operation.getProcess();
        Operation.F f = operation.getF();
        Operation.Type type = operation.getType();
        if (f == Operation.F.ADD) {
            if (type == Operation.Type.INVOKE) {
                elements.put(v, CheckerUtil.setFullElement(operation));
            } else {
                SetFullElement setFullElement = elements.get(v);
                setFullElement.setFullAdd(operation);
                elements.put(v, setFullElement);
            }
            return List.of(elements, reads, dups);
        } else if (f == Operation.F.READ) {
            if (type == Operation.Type.INVOKE) {
                reads.put(p, operation);
                return List.of(elements, reads, dups);
            } else if (type == Operation.Type.FAIL) {
                reads.remove(p);
                return List.of(elements, reads, dups);
            } else if (type == Operation.Type.INFO) {
                return List.of(elements, reads, dups);
            } else if (type == Operation.Type.OK) {
                Operation inv = reads.get(operation.getProcess());
                Map<Object, Integer> newDups = new TreeMap<>();
                List<?> valList;
                if (v instanceof List) {
                    valList = (List<?>) v;
                } else if(v instanceof HashSet){
                    valList= Arrays.asList(((HashSet<?>)v).toArray());
                }else {
                    valList = new ArrayList<>(List.of(v));
                }
                for (Object val : valList) {
                    if (!newDups.containsKey(val)) {
                        int times = Collections.frequency(valList, val);
                        if (times > 1) {
                            newDups.put(val, times);
                        }

                    }
                }


                dups.forEach((key, value) -> newDups.merge(key, (int) value, (v1, v2) -> v1 > v2 ? v1 : v2));

                HashSet<?> vSet = new HashSet<>(valList);
                for (Object element : elements.keySet()) {
                    SetFullElement state = elements.get(element);
                    if (vSet.contains(element)) {
                        elements.put(element, state.setFullReadPresent(inv, operation));
                    } else {
                        elements.put(element, state.setFullReadAbsent(inv, operation));
                    }
                }

                //                Function<Object[], Object[]> updateAll = o -> {
                //                    Map element = (Map) o[0];
                //                    ISetFullElement state = (ISetFullElement) o[1];
                //                    if (element.containsKey(finalV)) {
                //                        state.setFullReadPresent(inv, operation);
                //                    } else {
                //                        state.setFullReadAbsent(inv, operation);
                //                    }
                //                    return new Object[]{element, state};
                //                };
                //                elements = Util.mapKv(updateAll, elements);
                return List.of(elements, reads, newDups);
            }
        }
        return List.of(elements, reads, dups);
    }

}
