package core.checker.checker;

import core.checker.vo.Result;

import java.util.List;
import java.util.Map;

public interface Checker {

    /**
     * @param history
     * @param opts    a map of options controlling checker execution
     */
    Result check(Map test, List<Operation> history, Map opts);
}
