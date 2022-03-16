package core.checker;

import java.util.List;
import java.util.Map;

public interface Checker {

    /**
     * @param history
     * @param opts a map of options controlling checker execution
     */
    Map check(Map test, List<Map> history, Map opts);
}
