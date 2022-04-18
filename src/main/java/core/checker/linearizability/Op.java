package core.checker.linearizability;

import core.checker.checker.Operation;
import core.client.ClientInvokeResponse;
import core.record.ActionEnum;
import example.write_client.RWRequest;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

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
        super(op);
    }

    public Op(core.record.Operation op) {
        this.process = Integer.parseInt(op.getRequestId().toString() + op.getThreadId().toString());
        ActionEnum action = op.getAction();
        if (action == ActionEnum.InvokeOperation) {
            this.type = Type.INVOKE;
            RWRequest data = (RWRequest) op.getData();
            if (data.getAction().equals("read")) {
                this.f = F.READ;
            } else if (data.getAction().equals("write")) {
                this.f = F.WRITE;
            }
            this.value = data.getValue();
        } else if (action == ActionEnum.ResponseOperation) {
            ClientInvokeResponse data = (ClientInvokeResponse) op.getData();
            if (data.isSuccess()) {
                this.type = Type.OK;
            } else {
                this.type = Type.FAIL;
            }
            this.value = data.getNewState();
        }
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


}
