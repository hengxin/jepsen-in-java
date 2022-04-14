package core.checker.linearizability;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {
    List<Op> ops;
    Op lastWrite;
}
