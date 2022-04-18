package core.checker.checker;

import lombok.Data;

/**
 * Tracks the state of each element for set-full analysis
 */
@Data
public class SetFullElement implements ISetFullElement {
    private Object element;
    private Operation known;
    private Operation lastPresent;
    private Operation lastAbsent;

    public SetFullElement(Object element) {
        this.element = element;
    }

    public SetFullElement(Object element, Operation known, Operation lastPresent, Operation lastAbsent) {
        this.element = element;
        this.known = known;
        this.lastPresent = lastPresent;
        this.lastAbsent = lastAbsent;
    }

    @Override
    public ISetFullElement setFullAdd(Operation operation) {
        if (operation.getType()== Operation.Type.OK) {
            if (this.known == null) {
                this.known = operation;
            }
        }
        return this;
    }

    @Override
    public SetFullElement setFullReadPresent(Operation iop, Operation operation) {
        if (this.known == null) {
            this.known = operation;
        }
        if (this.lastPresent == null || this.lastPresent.getIndex() < iop.getIndex()) {
            this.lastPresent = iop;
        }
        return this;
    }

    @Override
    public SetFullElement setFullReadAbsent(Operation iop, Operation operation) {
        if (this.lastAbsent == null || this.lastAbsent.getIndex() < iop.getIndex()) {
            {
                this.lastAbsent = iop;
            }
        }
        return this;
    }


}
