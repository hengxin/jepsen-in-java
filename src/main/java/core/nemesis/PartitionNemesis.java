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

    public static String ALL_IPS = "AllIPs";
    public static String OTHER_ZONES_COUNT = "OtherZonesCount";

    @Override
    public Exception Invoke(Zone zone, Map<String, String> invokeArgs) {
        String allIPs = invokeArgs.get(ALL_IPS);
        String format = "iptables -I INPUT -s %s -j DROP";
        StringBuilder command = new StringBuilder();
        for(String ip: allIPs.split(" "))
            command.append(String.format(format, ip)).append("\n");
        return Support.ExecuteCommand(zone, command.toString());
    }

    @Override
    public Exception Recover(Zone zone, Map<String, String> recoverArgs) {
        String command = "iptables -D INPUT 1";
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < Integer.parseInt(recoverArgs.get(OTHER_ZONES_COUNT)); i++)
            stringBuilder.append(command).append("\n");
        return Support.ExecuteCommand(zone, command);
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
        Duration duration = Duration.ofMinutes(1).plusSeconds(new Random().nextInt(60));    // 默认至少一分钟最多两分钟
        switch (this.kind){
            case NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION:
                Zone leaderZone = null;
                String leaderIP = CheckLeader(zones.get(0));      // 随便一个zone都能用来查主，但实际上这步是有出错可能的，因为不知道这个zones是不是为空
                if(!leaderIP.equals("")){
                    StringBuilder allIPs = new StringBuilder();
                    for(Zone zone: zones) {
                        String ip = zone.getIP();
                        if(ip.equals(leaderIP))
                            leaderZone = zone;
                        allIPs.append(ip).append(" ");
                    }
                    Map<String, String> invokeArgs = new HashMap<>();
                    Map<String, String> recoverArgs = new HashMap<>();
                    invokeArgs.put(PartitionNemesis.ALL_IPS, allIPs.toString().strip());
                    recoverArgs.put(PartitionNemesis.OTHER_ZONES_COUNT, String.valueOf(zones.size() - 1));
                    operations.add(new NemesisOperation(NEMESIS_PARTITION_NODE, leaderZone, duration, invokeArgs, recoverArgs));
                }
                break;
            default:
        }
        return operations;
    }

    private String CheckLeader(Zone zone) {
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
