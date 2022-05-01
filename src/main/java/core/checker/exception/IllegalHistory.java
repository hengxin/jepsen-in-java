package core.checker.exception;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class IllegalHistory extends Exception {
    private List<Map<?, ?>> reads;
    private Object key;

    public IllegalHistory(String message) {
        super(message);
    }

    public IllegalHistory(List<Map<?, ?>> reads, String message) {
        super(message);
        this.reads = reads;
    }

    public IllegalHistory(List<Map<?, ?>> reads, Object k, String message) {
        super(message);
        this.reads = reads;
        this.key = k;
    }


}
