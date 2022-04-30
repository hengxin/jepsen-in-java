package core.nemesis;

import core.db.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.util.Map;

@Data
@AllArgsConstructor
public class NemesisOperation {

    private String nemesisName;
    private Node node;
    private Duration runTime;
    private Map<String, String> invokeArgs;
    private Map<String, String> recoverArgs;

}
