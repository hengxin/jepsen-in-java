package core.db;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

import static util.Support.*;

@Slf4j
public class Cassandra implements DB{

    @Override
    public Exception SetUp(Zone zone) {
        log.info("Setting up Cassandra in " + zone.getIp());
        Exception exception = SendFile(zone, "src/main/resources/cassandra/cassandra.repo", "/etc/yum.repos.d/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = SendFile(zone, "src/main/resources/cassandra/download.sh", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = ExecuteCommand(zone, ShellCommand("/root/download.sh", ""));        // 下载可能会非常慢
        if(exception != null)
            log.error(exception.getMessage());
        return null;
    }

    @Override
    public Exception TearDown(Zone zone) {
        return null;
    }

    @Override
    public String Name() {
        return "Cassandra";
    }

    @Override
    public Exception SetConfig(ArrayList<Zone> zones) {
        StringBuilder seedsBuilder = new StringBuilder();
        for(Zone zone: zones)
            seedsBuilder.append(zone.getIp()).append(",");
        String seeds = seedsBuilder.substring(0, seedsBuilder.length() - 1);
        String cluster_name = "GraduationDesign";       // TODO 自定义？不能有空格不然shell参数格式有误
        for(Zone zone: zones) {
            Exception exception = SendFile(zone, "src/main/resources/cassandra/cluster_config.sh", "/root/");
            if(exception != null)
                log.error(exception.getMessage());
            String args = cluster_name + " " + zone.getIp() + " " + seeds;
            exception = ExecuteCommand(zone, ShellCommand("/root/cluster_config.sh", args));
            if(exception != null)
                log.error(exception.getMessage());
        }
        return null;
    }
}
