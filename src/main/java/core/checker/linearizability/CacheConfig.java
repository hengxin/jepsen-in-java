package core.checker.linearizability;

import core.checker.checker.Operation;
import core.checker.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.BitSet;
import java.util.Objects;

@Data
@AllArgsConstructor
public class CacheConfig {
    private BitSet linearized;
    private Model model;
    private Op operation;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheConfig that = (CacheConfig) o;
        return Objects.equals(linearized, that.linearized) && Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linearized, model);
    }
}
