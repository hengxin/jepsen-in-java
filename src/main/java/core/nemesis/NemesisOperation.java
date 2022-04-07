package core.nemesis;

import core.db.Zone;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.util.Map;

@Data
@AllArgsConstructor
public class NemesisOperation {

    private String nemesisName;
    private Zone zone;
    private Duration runTime;
    private Map<String, String> invokeArgs;
    private Map<String, String> recoverArgs;

}
