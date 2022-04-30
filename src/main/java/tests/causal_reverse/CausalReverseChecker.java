package tests.causal_reverse;

import core.checker.checker.Checker;
import core.checker.checker.Operation;
import core.checker.vo.Result;

import java.util.List;
import java.util.Map;
import java.util.Set;

class CausalReverseChecker implements Checker {

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        Map<?, Set<Object>> expected = CausalReverse.graph(history);
        List<Operation> errors = CausalReverse.errors(history, expected);
        Result result = new Result(errors.isEmpty());
        result.setError(errors);
        return result;
    }
}
