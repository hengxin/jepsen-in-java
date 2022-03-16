package core.checker;

import java.util.List;
import java.util.Map;


/**
 * An empty checker that only returns nil.
 */
public class Noop implements Checker {

    @Override
    public Map check(Map test, List<Map> history, Map opts) {
        return null;
    }

}
