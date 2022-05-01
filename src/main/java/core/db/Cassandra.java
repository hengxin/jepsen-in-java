package core.db;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

import static util.Support.*;

@Slf4j
public class Cassandra implements DB {

    @Override
    public Exception SetUp(Node node) {
        log.info("Setting up Cassandra in " + node.getIp());
        Exception exception = SendFile(node, "src/main/resources/cassandra/cassandra.repo", "/etc/yum.repos.d/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = SendFile(node, "src/main/resources/cassandra/download.sh", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = ExecuteCommand(node, ShellCommand("/root/download.sh", ""));        // 下载可能会非常慢
        if(exception != null)
            log.error(exception.getMessage());
        return null;
    }

    @Override
    public Exception TearDown(Node node) {
        return null;
    }

    @Override
    public String Name() {
        return "Cassandra";
    }

    @Override
    public Exception SetConfig(ArrayList<Node> nodes) {
        StringBuilder seedsBuilder = new StringBuilder();
        for(Node node : nodes)
            seedsBuilder.append(node.getIp()).append(",");
        String seeds = seedsBuilder.substring(0, seedsBuilder.length() - 1);
        String cluster_name = "GraduationDesign";       // TODO 自定义？不能有空格不然shell参数 格式有误
        for(Node node : nodes) {
            Exception exception = SendFile(node, "src/main/resources/cassandra/cluster_config.sh", "/root/");
            if(exception != null)
                log.error(exception.getMessage());
            String args = cluster_name + " " + node.getIp() + " " + seeds;
            exception = ExecuteCommand(node, ShellCommand("/root/cluster_config.sh", args));
            if(exception != null)
                log.error(exception.getMessage());
        }
        return null;
    }
}
