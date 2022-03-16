package core.nemesis;

import core.db.Zone;

import java.time.Duration;

public class NemesisOperation {
    private String nemesisName;
    private Zone zone;
    private Duration runTime;

    public NemesisOperation(String name, Zone zone, Duration runTime) {
        this.nemesisName = name;
        this.zone = zone;
        this.runTime = runTime;
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
}
