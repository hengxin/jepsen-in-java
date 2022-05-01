package tests.causal;

import core.checker.checker.Operation;
import core.checker.model.Inconsistent;
import core.checker.model.Model;

class CausalRegister implements Model {
    private Object value;
    private int counter;
    private Object lastPos;

    CausalRegister(Object value, int counter, Object lastPos) {
        this.value = value;
        this.counter = counter;
        this.lastPos = lastPos;
    }

    @Override
    public Model step(Operation operation) {
        int c = counter + 1;
        Object newV = operation.getValue();
        int pos = operation.getPosition();
        Object link = operation.getLink();
        if (!(link.equals("init") || link.equals(lastPos))) {
            return new Inconsistent("Cannot link " + link + " to last-seen position " + lastPos);
        }
        Operation.F f = operation.getF();
        switch (f) {
            case WRITE:
                if (newV.equals(c)) {
                    return new CausalRegister(newV, c, pos);
                } else {
                    return new Inconsistent("expected value " + c + " attempting to write " + newV + " instead");
                }
            case READ_INIT:
                if (counter == 0 && !newV.equals(0)) {
                    return new Inconsistent("expected init value 0, read " + newV);
                }
                if (newV == null || newV.equals(value)) {
                    return new CausalRegister(value, counter, pos);
                }
                return new Inconsistent("can't read " + newV + " from register " + value);

            case READ:
                if (newV == null || newV.equals(value)) {
                    return new CausalRegister(value, counter, pos);
                }
                return new Inconsistent("can't have " + newV + " from register " + value);
        }

        return this;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}