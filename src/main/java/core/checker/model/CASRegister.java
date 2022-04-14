package core.checker.model;

import clojure.lang.LispReader;
import core.checker.checker.Operation;
import core.checker.linearizability.CacheConfig;
import lombok.Data;
import lombok.val;

import java.util.List;

@Data
public class CASRegister implements Model {
    private Object value;

    public CASRegister(Object value) {
        this.value = value;
    }


    @Override
    public Model step(Operation op) {
        Operation.F f = op.getF();
        if (f == Operation.F.WRITE) {
            return new CASRegister(op.getValue());
        } else if (f == Operation.F.CAS) {
            List<?> vs = (List<?>) op.getValue();
            Object cur = vs.get(0);
            Object newVal = vs.get(1);
            if (cur == value) {
                return new CASRegister(newVal);
            } else {
                return new Inconsistent("can't CAS " + value + " from " + cur + " to " + newVal);
            }
        } else if (f == Operation.F.READ) {
            if (op.getValue() == null || op.getValue() == value) {
                return this;
            } else {
                return new Inconsistent("can't read " + op.getValue() + " from register " + value);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
