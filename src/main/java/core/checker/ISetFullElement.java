package core.checker;

import java.util.Map;

public interface ISetFullElement {
    public ISetFullElement setFullAdd(Map op);

    public ISetFullElement setFullReadPresent(Map inv, Object op);

    public ISetFullElement setFullReadAbsent(Map inv, Object op);
}
