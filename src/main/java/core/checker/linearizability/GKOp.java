package core.checker.linearizability;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GKOp {
    private int process;
    private String type;
    private String f;
    private int value;
    private int idx;
    private double start;
    private double end;

    public GKOp(int process, String f, int value, double start, double end) {
        this.process = process;
        this.f = f;
        this.value = value;
        this.start = start;
        this.end = end;
    }

    public GKOp(int process, String f, String type, int value) {
        this.process = process;
        this.f = f;
        this.type = type;
        this.value = value;
    }

    public enum F{

    }


}
