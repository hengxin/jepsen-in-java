package core.nemesis;

import core.db.Zone;

import java.time.Duration;
import java.util.ArrayList;

public class DelayNemesisGenerator implements NemesisGenerator {
    private NemesisGenerator generator;
    private Duration duration;

    @Override
    public ArrayList<NemesisOperation> Generate(ArrayList<Zone> zones) {
        duration = Duration.ofMinutes(1);
        try {
            Thread.sleep(duration.toMillis());      // 直接把time.duration的值变成毫秒级给sleep()
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generator.Generate(zones);
    }
}
