package core.checker.checker;

import core.checker.vo.Result;

import java.util.List;
import java.util.Map;


/**
 * An empty checker that only returns nil.
 */
public class Noop implements Checker {

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        return null;
    }

}
