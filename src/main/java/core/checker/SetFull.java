package core.checker;

import util.Util;

import java.util.*;
import java.util.function.Function;

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
    public Map check(Map test, List<Map> history, Map opts) {
        history = (List<Map>) history.stream().filter(h -> h.get("process") instanceof Number);
        Map elements = Map.of(), reads = Map.of(), dups = Map.of();
        for (Map op : history) {
            List<Map> res = red(elements, reads, dups, op);
            elements = res.get(0);
            reads = res.get(1);
            dups = res.get(2);
        }
        elements = new TreeMap(elements);

        Map setResults = CheckerUtil.setFullResults(checkerOpts, (List<SetFullElement>) elements.values());
        setResults.put("valid", dups.isEmpty() && (boolean) setResults.get("valid?"));
        setResults.put("duplicated-count", dups.size());
        setResults.put("duplicated", dups);
        return setResults;
    }

    private List<Map> red(Map elements, Map reads, Map dups, Map op) {
        List v = (List) op.get("value");
        Object p = op.get("process");
        Object f = op.get("f");
        Object type = op.get("type");
        SetFullElement setFullElement = new SetFullElement(op); //TODO check
        if (f.equals("add")) {
            if (type.equals("invoke")) {
                elements.put(v, CheckerUtil.setFullElement(op));
            } else {
                elements.put(v, setFullElement.setFullAdd(op));
            }
            return List.of(elements, reads, dups);
        } else if (f.equals("read")) {
            if (type.equals("invoke")) {
                reads.put(p, op);
                return List.of(elements, reads, dups);
            } else if (type.equals("fail")) {
                reads.remove(p);
                return List.of(elements, reads, dups);
            } else if (type.equals("info")) {
                return List.of(elements, reads, dups);
            } else if (type.equals("ok")) {
                Map inv = (Map) reads.get(op.get("process"));
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
                        state.setFullReadPresent(inv, op);
                    } else {
                        state.setFullReadAbsent(inv, op);
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
