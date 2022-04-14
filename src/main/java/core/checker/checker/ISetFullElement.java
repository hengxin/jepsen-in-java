package core.checker.checker;

import java.util.Map;

public interface ISetFullElement {
    public ISetFullElement setFullAdd(Operation operation);

    public ISetFullElement setFullReadPresent(Map inv, Operation operation);

    public ISetFullElement setFullReadAbsent(Map inv, Operation operation);
}
