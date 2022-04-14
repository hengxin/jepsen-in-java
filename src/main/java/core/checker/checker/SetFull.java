package core.checker.checker;

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
    boolean linearizable = false;
    private Map checkerOpts;

    public SetFull(Map checkerOpts) {
        this.checkerOpts = checkerOpts;
    }

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        history = history.stream().filter(h -> h.getProcess() >= 0).collect(Collectors.toList());
        Map elements = Map.of(), reads = Map.of(), dups = Map.of();
        for (Operation operation : history) {
            List<Map> res = red(elements, reads, dups, operation);
            elements = res.get(0);
            reads = res.get(1);
            dups = res.get(2);
        }
        elements = new TreeMap(elements);

        Map<String, Object> setResults = CheckerUtil.setFullResults(checkerOpts, (List<SetFullElement>) elements.values());
        setResults.put("valid", dups.isEmpty() && (boolean) setResults.get("valid?"));
        setResults.put("duplicated-count", dups.size());
        setResults.put("duplicated", dups);
        Result result = new Result();
        result.setValid(dups.isEmpty() && (boolean) setResults.get("valid?"));
        result.setRes(setResults);
        return result;
    }

    private List<Map> red(Map elements, Map reads, Map dups, Operation operation) {
        List v = (List) operation.getValue();
        Object p = operation.getProcess();
        Object f = operation.getF();
        Object type = operation.getType();
        SetFullElement setFullElement = new SetFullElement(operation); //TODO check
        if (f.equals("add")) {
            if (type.equals("invoke")) {
                elements.put(v, CheckerUtil.setFullElement(operation));
            } else {
                elements.put(v, setFullElement.setFullAdd(operation));
            }
            return List.of(elements, reads, dups);
        } else if (f.equals("read")) {
            if (type.equals("invoke")) {
                reads.put(p, operation);
                return List.of(elements, reads, dups);
            } else if (type.equals("fail")) {
                reads.remove(p);
                return List.of(elements, reads, dups);
            } else if (type.equals("info")) {
                return List.of(elements, reads, dups);
            } else if (type.equals("ok")) {
                Map inv = (Map) reads.get(operation.getProcess());
                Map<Object, Integer> frequencies = new HashMap();
                for (Object value : v) {
                    if (!frequencies.containsKey(value)) {
                        frequencies.put(value, Collections.frequency(v, value));
                    }
                }

                //TODO check
                Map<Object, Integer> newDups = new TreeMap();
                for (Map.Entry<Object, Integer> entry : frequencies.entrySet()) {
                    Object k = entry.getKey();
                    Integer value = entry.getValue();
                    if (value < 1) {
                        newDups.put(k, value);
                    }
                }

                dups.forEach((key, value) -> newDups.merge(key, (int) value, (v1, v2) -> v1 > v2 ? v1 : v2));

                v = Arrays.asList(new HashSet(v).toArray());
                List finalV = v;
                Function<Object[], Object[]> updateAll = o -> {
                    Map element = (Map) o[0];
                    ISetFullElement state = (ISetFullElement) o[1];
                    if (element.containsKey(finalV)) {
                        state.setFullReadPresent(inv, operation);
                    } else {
                        state.setFullReadAbsent(inv, operation);
                    }
                    return new Object[]{element, state};
                };
                elements = Util.mapKv(updateAll, elements);
                return List.of(elements, reads, newDups);
            }
        }
        return List.of(elements, reads, dups);
    }

}
