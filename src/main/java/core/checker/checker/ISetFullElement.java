package core.checker.checker;

public interface ISetFullElement {
    public ISetFullElement setFullAdd(Operation operation);

    public ISetFullElement setFullReadPresent(Operation inv, Operation operation);

    public ISetFullElement setFullReadAbsent(Operation inv, Operation operation);
}
