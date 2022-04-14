package core.checker.model;

import core.checker.checker.Operation;
import lombok.Data;

@Data
public class Inconsistent implements Model {
    private Object msg;

    public Inconsistent(Object msg) {
        this.msg = msg;
    }

    @Override
    public Model step(Operation operation) {
        return this;
    }


    @Override
    public String toString() {
        return "Inconsistent{" +
                "msg=" + msg +
                '}';
    }
}
