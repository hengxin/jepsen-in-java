package tests;

import core.checker.checker.Operation;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Invoke extends Operation {
    private Type type;
    private F f;
    private Object value;

    public Invoke(Type type, F f, Object value) {
        this.type = type;
        this.f = f;
        this.value = value;
    }

    public Invoke(F f, Object value) {
        this.type = Type.INVOKE;
        this.f = f;
        this.value = value;
    }
}
