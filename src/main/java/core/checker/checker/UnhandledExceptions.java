package core.checker.checker;

import core.checker.vo.Result;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Returns information about unhandled exceptions: a sequence of maps sorted in
 * descending frequency order
 */
public class UnhandledExceptions implements Checker {

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        List<Map<String, Object>> exes = history.stream()
                .filter(Operation::isException)
                .filter(h -> h.getType() == Operation.Type.INFO)
                .collect(Collectors.groupingBy(h -> {
                    if (h.getException().getCause() == null) {
                        return h.getException().getClass();
                    } else {
                        return h.getException().getCause().getClass();
                    }

                }))
                .values()
                .stream().sorted((v1, v2) -> v2.size() - v1.size())
                .map(ops -> {
                    Operation operation = ops.get(0);
                    Exception e = operation.getException();
                    Object causeClass = e.getCause() == null ? e.getClass() : e.getCause().getClass();
                    return Map.of(
                            "count", ops.size(),
                            "class", causeClass,
                            "example", operation);
                }).collect(Collectors.toList());

        if (exes.size() > 0) {
            Result result = new Result();
            result.setValid(true);
            result.setExceptions(exes);
            return result;
        } else {
            Result result = new Result();
            result.setValid(true);
            return result;
        }

    }
}
