package core.checker.linearizability;

import core.checker.model.Model;
import lombok.Data;

@Data
public abstract class Wrapper {
    private Model model;
}
