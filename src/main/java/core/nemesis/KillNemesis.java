package core.nemesis;

import core.db.Zone;
import util.Support;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

import static util.Constant.*;


public class KillNemesis implements Nemesis {
    @Override
    public Exception Invoke(Zone zone, Map<String, String> invokeArgs) {
        String command = "pidof observer | xargs kill -STOP";
        return Support.ExecuteCommand(zone, command);
    }

    @Override
    public Exception Recover(Zone zone, Map<String, String> recoverArgs) {
        String command = "pidof observer | xargs kill -CONT";
        return Support.ExecuteCommand(zone, command);
    }

    @Override
    public String Name() {
        return NEMESIS_KILL_NODE;
    }
}

class KillGenerator implements NemesisGenerator {

    private String kind;

    KillGenerator(String kind) {
        this.kind = kind;
    }

    @Override
    public ArrayList<NemesisOperation> Generate(ArrayList<Zone> zones) {
        int n = 0;
        Duration duration = Duration.ofSeconds(8).plusSeconds(new Random().nextInt(5));
//        Duration duration = Duration.ofMinutes(1).plusSeconds(new Random().nextInt(60));    // 默认至少一分钟最多两分钟
        switch (this.kind){
            case NEMESIS_GENERATOR_ALL_KILL:
                n = zones.size();
                break;
            case NEMESIS_GENERATOR_RANDOM_KILL:
                n = 1;
                break;
            default:
        }
        return this.KillNodes(zones, n, duration);
    }

    private ArrayList<NemesisOperation> KillNodes(ArrayList<Zone> zones, int n, Duration duration) {
        ArrayList<NemesisOperation> operations = new ArrayList<>();

        // shuffle indices
        int length = zones.size();
        ArrayList<Integer> indices = new ArrayList<>();
        for(int i = 0; i < length; i++)
            indices.add(i);
        Collections.shuffle(indices);       // 从后往前用一个随机数做index进行swap
        if(n > length)
            n = length;

        for(int i = 0; i < n; i++)
            operations.add(new NemesisOperation(NEMESIS_KILL_NODE, zones.get(indices.get(i)), duration, null, null));
        return operations;
    }
}
