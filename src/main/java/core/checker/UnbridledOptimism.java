package core.checker;

import java.util.List;
import java.util.Map;

/**
 * everything is awesome
 */
public class UnbridledOptimism implements Checker{

    @Override
    public Map check(Map test, List<Map> history, Map opts) {
        return Map.of("valid?",true);
    }
}
