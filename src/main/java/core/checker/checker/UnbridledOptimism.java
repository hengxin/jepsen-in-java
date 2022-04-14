package core.checker.checker;

import core.checker.vo.Result;

import java.util.List;
import java.util.Map;

/**
 * everything is awesome
 */
public class UnbridledOptimism implements Checker {

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {

        Result result = new Result();
        result.setValid(true);
        return result;
    }
}
