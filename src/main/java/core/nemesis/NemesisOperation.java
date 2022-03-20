package core.nemesis;

import core.db.Zone;

import java.time.Duration;
import java.util.Map;

public class NemesisOperation {
    private String nemesisName;
    private Zone zone;
    private Duration runTime;
    private Map<String, String> invokeArgs;
    private Map<String, String> recoverArgs;


    public NemesisOperation(String name, Zone zone, Duration runTime, Map<String, String> invokeArgs, Map<String, String> recoverArgs) {
        this.nemesisName = name;
        this.zone = zone;
        this.runTime = runTime;
        this.invokeArgs = invokeArgs;
        this.recoverArgs = recoverArgs;
    }

    public String getNemesisName() {
        return nemesisName;
    }

    public Zone getZone() {
        return zone;
    }

    public Duration getRunTime() {
        return runTime;
    }

    public Map<String, String> getInvokeArgs() {
        return invokeArgs;
    }

    public Map<String, String> getRecoverArgs() {
        return recoverArgs;
    }
}
