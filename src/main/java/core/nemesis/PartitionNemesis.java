package core.nemesis;

import core.db.Zone;
import lombok.extern.slf4j.Slf4j;
import util.Support;

import java.sql.ResultSet;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import static util.Constant.*;



public class PartitionNemesis implements Nemesis {

    public static String OTHER_IPS = "OtherIPs";
    public static String OTHER_ZONES_COUNT = "OtherZonesCount";

    @Override
    public Exception Invoke(Zone zone, Map<String, String> invokeArgs) {
        String allIPs = invokeArgs.get(OTHER_IPS);
        String format = "iptables -I INPUT -s %s -j DROP";
        StringBuilder command = new StringBuilder();
        for(String ip: allIPs.split(" "))
            command.append(String.format(format, ip)).append("\n");
        return Support.ExecuteCommand(zone, command.toString());
    }

    @Override
    public Exception Recover(Zone zone, Map<String, String> recoverArgs) {
        String command = "iptables -D INPUT 1";
        StringBuilder commandBuilder = new StringBuilder();
        for(int i = 0; i < Integer.parseInt(recoverArgs.get(OTHER_ZONES_COUNT)); i++)
            commandBuilder.append(command).append("\n");
        return Support.ExecuteCommand(zone, commandBuilder.toString());
    }

    @Override
    public String Name() {
        return NEMESIS_PARTITION_NODE;
    }
}

@Slf4j
class PartitionGenerator implements NemesisGenerator {

    private String kind;

    PartitionGenerator(String kind) {
        this.kind = kind;
    }

    @Override
    public ArrayList<NemesisOperation> Generate(ArrayList<Zone> zones) {
        ArrayList<NemesisOperation> operations = new ArrayList<>();
        Duration duration = Duration.ofMinutes(4).plusSeconds(new Random().nextInt(30));    // 默认至少一分钟最多两分钟
        int size = zones.size();
        if(size == 0)
            return operations;
        String leaderIP = CheckLeaderIP(zones.get(0));      // 随便一个zone都能用来查主
        log.info("Leader ip is " + leaderIP + ".");
        ArrayList<Integer> shuffledIndices = Support.ShuffleByCount(size);
        Map<String, String> invokeArgs = new HashMap<>();
        Map<String, String> recoverArgs = new HashMap<>();
        switch (this.kind){
            // random one node <-x-> all else
            case NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION:
                Zone selectedZone = zones.get(shuffledIndices.get(0));
                StringBuilder otherIPs = new StringBuilder();
                for(int i = 1; i < size; i++)
                    otherIPs.append(zones.get(shuffledIndices.get(i)).getIP()).append(" ");
                invokeArgs.put(PartitionNemesis.OTHER_IPS, otherIPs.toString().strip());
                recoverArgs.put(PartitionNemesis.OTHER_ZONES_COUNT, String.valueOf(size - 1));
                operations.add(new NemesisOperation(NEMESIS_PARTITION_NODE, selectedZone, duration, invokeArgs, recoverArgs));
                break;
            // leader <-x-> random one else
            case NEMESIS_GENERATOR_ASYMMETRIC_NETWORK_PARTITION:
                Zone leaderZone = null;
                String randomIP = "";
                if(!leaderIP.equals("")) {
                    for(int i = 0; i < size && (leaderZone == null || randomIP.equals("")); i++) {
                        Zone zone = zones.get(shuffledIndices.get(i));
                        if(zone.getIP().equals(leaderIP))
                            leaderZone = zone;
                        else if(randomIP.equals(""))
                            randomIP = zone.getIP();
                    }
                    invokeArgs.put(PartitionNemesis.OTHER_IPS, randomIP);
                    recoverArgs.put(PartitionNemesis.OTHER_ZONES_COUNT, String.valueOf(1));
                    operations.add(new NemesisOperation(NEMESIS_PARTITION_NODE, leaderZone, duration, invokeArgs, recoverArgs));
                }
                break;
            default:
        }
        return operations;
    }

    private String CheckLeaderIP(Zone zone) {
        String sql = "select svr_ip from gv$partition where role = 1 limit 1";      // role=1为主副本，role=2为从副本
        Function<ResultSet, String> handle = (ResultSet rs) -> {
            try {
                rs.next();
                return rs.getString("svr_ip");
            } catch (Exception e) {
                log.error(e.getMessage());
                return "";
            }
        };
        return Support.JDBCQuery(zone, sql, handle);
    }
}
