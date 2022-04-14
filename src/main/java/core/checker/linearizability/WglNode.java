package core.checker.linearizability;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WglNode {
    private WglNode prev;
    private WglNode next;
    private Op op;
    private WglNode match;
}
