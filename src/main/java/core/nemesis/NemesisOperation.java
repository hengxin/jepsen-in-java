package core.nemesis;

import core.db.Zone;

import java.time.Duration;

public class NemesisOperation {
    private String nemesisName;
    private Zone zone;


    // We have two approaches to trigger recovery
    // 1. through `RunTime`
    // 2. through `NemesisControl` WaitForRollback
    private Duration runTime;
    private NemesisControl recoverySignal;

    public NemesisOperation(String name, Zone zone, Duration runTime, NemesisControl recoverySignal) {
        this.nemesisName = name;
        this.zone = zone;
        this.runTime = runTime;
        this.recoverySignal = recoverySignal;
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

    public NemesisControl getRecoverySignal() {
        return recoverySignal;
    }
}
