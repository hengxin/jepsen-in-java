package core.checker.checker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operation {
    private int process;
    private boolean isNemesis;
    private Type type;
    private Object value;
    private F f;
    private boolean isException;
    private Exception exception;
    private Object error;
    private boolean isFail;
    private int subIndex;
    private double time;
    private double latency;
    private Operation completion;
    private int index;
    private Map<String, Double> clockOffsets;

    public Operation(Operation operation) {
        this.process = operation.getProcess();
        this.isNemesis = operation.isNemesis;
        this.type = operation.getType();
        this.value = operation.getValue();
        this.f = operation.getF();
        this.isException = operation.isException;
        this.exception = operation.getException();
        this.error = operation.getError();
        this.isFail = operation.isFail;
    }

    public Operation(int process, Type type, F f, Object value) {
        this.process = process;
        this.type = type;
        this.f = f;
        this.value = value;
    }

    public Operation(int process, Type type, F f, Object value, Exception e, Object error) {
        this.process = process;
        this.type = type;
        this.f = f;
        this.value = value;
        this.isException = true;
        this.exception = e;
        this.error = error;
    }

    public Operation(F f, Type type) {
        this.f = f;
        this.type = type;
    }

    public Operation(int process, double time, Map<String, Double> clockOffsets) {
        this.process = process;
        this.time = time;
        this.clockOffsets = clockOffsets;
    }

    public static Operation invoke(int process, F f, Object value) {
        return new Operation(process, Type.INVOKE, f, value);
    }

    public static Operation ok(int process, F f, Object value) {
        return new Operation(process, Type.OK, f, value);
    }

    public static Operation fail(int process, F f, Object value) {
        return new Operation(process, Type.FAIL, f, value);
    }


    @Override
    public String toString() {
        return "Operation{" +
                "process=" + process +
                ", type=" + type +
                ", value=" + value +
                ", f=" + f +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return process == operation.process && index == operation.index && type == operation.type && Objects.equals(value, operation.value) && f == operation.f;
    }

    @Override
    public int hashCode() {
        return Objects.hash(process, type, value, f, index);
    }

    public enum Type {
        INVOKE, INFO, OK, FAIL
    }

    public enum F {
        START, STOP, READ, WRITE, CAS, ADD, TRANSFER, INC, TXN, RESUME_PD, RESUME_KV, RESUME_DB, START_PD, START_KV, START_DB,
        KILL_PD, KILL_KV, KILL_DB, PAUSE_DB, SHUFFLE_LEADER, SHUFFLE_REGION, RANDOM_MERGE, DEL_SHUFFLE_LEADER, DEL_SHUFFLE_REGION, DEL_RANDOM_MERGE,
        SLOW_PRIMARY, STOP_PARTITION, CREATE_TABLE, INSERT, DRAIN, DEQUEUE, ENQUEUE, GENERATE, FOO, BAR,RELEASE,ACQUIRE
    }
}
