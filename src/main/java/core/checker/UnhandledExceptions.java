package core.checker;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Returns information about unhandled exceptions: a sequence of maps sorted in
 * descending frequency order
 */
public class UnhandledExceptions implements Checker {

    @Override
    public Map check(Map test, List<Map> history, Map opts) {
        List exes = history.stream()
                .filter(h -> h.containsKey("exception"))
                .filter(h -> h.get("type").equals("info"))
                .collect(Collectors.groupingBy(h -> ((Exception) h.get("exception")).getCause().getClass()))
                .values()
                .stream().sorted((v1, v2) -> v2.size() - v1.size())
                .map(ops -> {
                    Map op = ops.get(0);
                    Exception e = (Exception) op.get("exception");
                    return Map.of(
                            "count", ops.size(),
                            "class", e.getCause().getClass(),
                            "example", op);
                }).collect(Collectors.toList());

        return Map.of(
                "valid?", true,
                "exceptions", exes
        );
    }

    private void tmp(List ops) {
        Map op = (Map) ops.get(0);
        Object e = op.get("exception");

    }
}
