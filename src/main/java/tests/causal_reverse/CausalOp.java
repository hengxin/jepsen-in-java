package tests.causal_reverse;

import core.checker.checker.Operation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class CausalOp extends Operation {
    private Set<Object> missing;
    private int expectedCount;

    public CausalOp(Operation op) {
        super(op);
    }
}
