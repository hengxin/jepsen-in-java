package core.checker;

import java.util.Map;

/**
 * Tracks the state of each element for set-full analysis
 */
public class SetFullElement implements ISetFullElement {
    Object element;
    Object known;
    Map lastPresent;
    Map lastAbsent;

    public SetFullElement(Object element) {
        this.element = element;
    }

    public SetFullElement(Object element, Object known, Map lastPresent, Map lastAbsent) {
        this.element = element;
        this.known = known;
        this.lastPresent = lastPresent;
        this.lastAbsent = lastAbsent;
    }

    @Override
    public ISetFullElement setFullAdd(Map op) {
        if (op.get("type").equals("ok")) {
            if (this.known == null || (this.known instanceof Boolean && !((boolean) this.known))) {
                this.known = op;
            }
        }
        return this;
    }

    @Override
    public ISetFullElement setFullReadPresent(Map iop, Object op) {
        if (this.known == null || (this.known instanceof Boolean && !((boolean) this.known))) {
            this.known = op;
        }
        if (this.lastPresent == null || (int) this.lastPresent.get("index") < (int) iop.get("index")) {
            this.lastPresent = iop;
        }
        return this;
    }

    @Override
    public ISetFullElement setFullReadAbsent(Map iop, Object op) {
        if (this.lastAbsent == null || (int) this.lastAbsent.get("index") < (int) iop.get("index")) {
            {
                this.lastAbsent = iop;
            }
        }
        return this;
    }


}
