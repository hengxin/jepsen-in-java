package core.checker.model;

import core.checker.checker.Operation;
import lombok.Data;

@Data
public class Mutex implements Model {
    private boolean locked;

    public Mutex(boolean locked) {
        this.locked = locked;
    }

    @Override
    public Model step(Operation operation) {
        Operation.F f = operation.getF();
        switch (f) {
            case ACQUIRE:
                if (locked) {
                    return new Inconsistent("already held");
                } else {
                    return new Mutex(true);
                }
            case RELEASE:
                if (locked) {
                    return new Mutex(false);
                } else {
                    return new Inconsistent("not held");
                }
        }
        return this;
    }

    @Override
    public String toString() {
        return locked ? "locked" : "free";
    }
}
