package core.checker.linearizability;

import core.checker.checker.Operation;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.Objects;

@Slf4j
@Data
@NoArgsConstructor
public class Op extends Operation {
    private int process;
    private Type type;
    private F f;
    private Object value;
    private int index = -1;
    private int entryId = -1;
    private Object m;
    private double start;
    private double end;

    public Op(int process, F f, int value, double start, double end) {
        this.process = process;
        this.f = f;
        this.value = value;
        this.start = start;
        this.end = end;
    }

    public Op(int process, F f, Type type, Object value) {
        this.process = process;
        this.f = f;
        this.type = type;
        this.value = value;
    }

    public Op(int process, F f, Type type, Object value, int index) {
        this.process = process;
        this.f = f;
        this.type = type;
        this.value = value;
        this.index = index;
    }

    public Op(Object process, Object f, Object type, Object value) {
        if (process instanceof Number) {
            this.process = Integer.parseInt(process.toString());
        } else {
            this.process = -1;
        }

        this.f = F.valueOf(f.toString().substring(1).toUpperCase(Locale.ROOT));
        this.type = Type.valueOf(type.toString().substring(1).toUpperCase(Locale.ROOT));
        this.value = value;
    }

    public Op(Op op) {
        this.process = op.getProcess();
        this.f = op.getF();
        this.type = op.getType();
        this.value = op.getValue();
        this.index = op.getIndex();
    }
    public Op(Operation op){
        this.process = op.getProcess();
        this.f = op.getF();
        this.type = op.getType();
        this.value = op.getValue();
        this.index = op.getIndex();
    }


    @Override
    public String toString() {
        return "Op{" +
                "process=" + process +
                ", type=" + type +
                ", f=" + f +
                ", value=" + value +
                ", index=" + index +
                ", entryId=" + entryId +
                ", m=" + m +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Op op = (Op) o;
        return process == op.process && index == op.index&& type == op.type && f == op.f && Objects.equals(value, op.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), process, type, f, value, index);
    }
}
