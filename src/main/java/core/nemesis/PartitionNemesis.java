package core.nemesis;

import core.db.Node;
import lombok.extern.slf4j.Slf4j;
import util.Support;

import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static util.Constant.*;


public class PartitionNemesis implements Nemesis {

    public static String OTHER_IPS = "OtherIPs";
    public static String OTHER_NODES_COUNT = "OtherNodesCount";

    @Override
    public Exception Invoke(Node node, Map<String, String> invokeArgs) {
        String allIPs = invokeArgs.get(OTHER_IPS);
        String format = "iptables -I INPUT -s %s -j DROP";
        StringBuilder command = new StringBuilder();
        for(String ip: allIPs.split(" "))
            command.append(String.format(format, ip)).append("\n");
        return Support.ExecuteCommand(node, command.toString());
    }

    @Override
    public Exception Recover(Node node, Map<String, String> recoverArgs) {
        String command = "iptables -D INPUT 1";
        StringBuilder commandBuilder = new StringBuilder();
        for(int i = 0; i < Integer.parseInt(recoverArgs.get(OTHER_NODES_COUNT)); i++)
            commandBuilder.append(command).append("\n");
        return Support.ExecuteCommand(node, commandBuilder.toString());
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
    public ArrayList<NemesisOperation> Generate(ArrayList<Node> nodes) {
        ArrayList<NemesisOperation> operations = new ArrayList<>();
        Duration duration = Duration.ofMinutes(2).plusSeconds(new Random().nextInt(30));
        int size = nodes.size();
        if(size == 0)
            return operations;
        String leaderIP = CheckLeaderIP(nodes.get(0));      // 随便一个node都能用来查主
        log.info("Leader ip is " + leaderIP + ".");
        ArrayList<Integer> shuffledIndices = Support.ShuffleByCount(size);
        Map<String, String> invokeArgs = new HashMap<>();
        Map<String, String> recoverArgs = new HashMap<>();
        switch (this.kind){
            // random one node <-x-> all else
            case NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION:
                Node selectedNode = nodes.get(shuffledIndices.get(0));
                StringBuilder otherIPs = new StringBuilder();
                for(int i = 1; i < size; i++)
                    otherIPs.append(nodes.get(shuffledIndices.get(i)).getIp()).append(" ");
                invokeArgs.put(PartitionNemesis.OTHER_IPS, otherIPs.toString().strip());
                recoverArgs.put(PartitionNemesis.OTHER_NODES_COUNT, String.valueOf(size - 1));
                operations.add(new NemesisOperation(NEMESIS_PARTITION_NODE, selectedNode, duration, invokeArgs, recoverArgs));
                break;
            // leader <-x-> random one else
            case NEMESIS_GENERATOR_ASYMMETRIC_NETWORK_PARTITION:
                Node leaderNode = null;
                String randomIP = "";
                if(!leaderIP.equals("")) {
                    for(int i = 0; i < size && (leaderNode == null || randomIP.equals("")); i++) {
                        Node node = nodes.get(shuffledIndices.get(i));
                        if(node.getIp().equals(leaderIP))
                            leaderNode = node;
                        else if(randomIP.equals(""))
                            randomIP = node.getIp();
                    }
                    invokeArgs.put(PartitionNemesis.OTHER_IPS, randomIP);
                    recoverArgs.put(PartitionNemesis.OTHER_NODES_COUNT, String.valueOf(1));
                    operations.add(new NemesisOperation(NEMESIS_PARTITION_NODE, leaderNode, duration, invokeArgs, recoverArgs));
                }
                break;
            default:
        }
        return operations;
    }

    private String CheckLeaderIP(Node node) {
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
        return Support.JDBCQueryWithNode(node, sql, handle);
    }
}
