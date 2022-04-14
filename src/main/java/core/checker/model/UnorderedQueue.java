package core.checker.model;

import com.google.common.collect.Multiset;
import core.checker.checker.Operation;

public class UnorderedQueue implements Model {
    private final Multiset<Object> pending;

    public UnorderedQueue(Multiset<Object> multiSet) {
        this.pending = multiSet;

    }

    @Override
    public Model step(Operation operation) {
        if (operation.getF() == Operation.F.ENQUEUE) {
            pending.add(operation.getValue());
        } else if (operation.getF() == Operation.F.DEQUEUE) {
            if (pending.contains(operation.getValue())) {
                pending.remove(operation.getValue());
            } else {
                return new Inconsistent("can't dequeue " + operation.getValue());
            }

        }
        return this;
    }
}
