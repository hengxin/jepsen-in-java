package core.checker.model;

import core.checker.checker.Operation;
import lombok.Data;

@Data
public class Register implements Model {
    private Object value;

    public Register(Object value) {
        this.value = value;
    }

    @Override
    public Model step(Operation operation) {
        Operation.F f = operation.getF();
        if (f == Operation.F.WRITE) {
            return new Register(operation.getValue());
        } else if (f == Operation.F.READ) {
            if (operation.getValue() == null || operation.getValue() == value) {
                return this;
            } else {
                return new Inconsistent(value + "≠" + operation.getValue());
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return value.toString();
    }


}
