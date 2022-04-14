package core.checker.model;

import core.checker.checker.Operation;

public interface Model {
    public Model step(Operation operation);
}
