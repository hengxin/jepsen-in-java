package core.checker.linearizability;

import core.checker.model.Model;
import lombok.Data;
import net.schmizz.sshj.transport.cipher.Cipher;

@Data
public abstract class Wrapper {
    private Model model;
}
