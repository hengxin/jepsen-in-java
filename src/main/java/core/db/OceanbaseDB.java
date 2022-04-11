package core.db;

import lombok.extern.slf4j.Slf4j;
import util.Support;

import java.util.ArrayList;


@Slf4j
public class OceanbaseDB implements DB {
    @Override
    public Exception SetUp(Zone zone) {
        log.info("Set up OceanbaseDB in" + zone.getIp());
        // Oceanbase使用obdeploy统一安装配置，所以这里不需要每个节点再单独下载了
        return null;
    }

    @Override
    public Exception TearDown(Zone zone) {
        return null;
    }

    @Override
    public String Name() {
        return "Oceanbase";
    }

    @Override
    public Exception SetConfig(ArrayList<Zone> zones) {
        if(zones.size() == 0)
            return new Exception("Zones is empty");
        String obcontrol = zones.get(0).getIp();            // 默认第一个节点为obcontrol
        StringBuilder observerBuilder = new StringBuilder();
        for(Zone zone: zones)
            observerBuilder.append(zone.getIp()).append(" ");
        String observer = observerBuilder.toString().strip();
        log.info("Set config for OceanbaseDB to all nodes");
        log.info("OBcontrol is " + obcontrol);
        log.info("OBserver is " + observer);

        BasicConfig(zones, obcontrol, observer);
        ChronyConfig(zones, obcontrol);
        SSH(zones.get(0));
        OBD(zones.get(0));
        return null;
    }

    private void BasicConfig(ArrayList<Zone> zones, String obcontrol, String observer) {
        String executeConfig = "chmod u+x /root/obconfig.sh\n" +
                "/root/obconfig.sh " + obcontrol + " " + observer;
        for(Zone zone: zones) {
            Exception exception = Support.SendFile(zone, "src/main/resources/oceanbase/obconfig.sh", "/root/");
            if(exception != null)
                log.error(exception.getMessage());
            exception = Support.ExecuteCommand(zone, executeConfig);
            if(exception != null)
                log.error(exception.getMessage());


            // download mysql for tenant
            exception = Support.SendFile(zone, "src/main/resources/mysql/mysql-community.repo", "/etc/yum.repos.d/");
            if(exception != null)
                log.error(exception.getMessage());
            exception = Support.SendFile(zone, "src/main/resources/mysql/centos8_mysql.sh", "/root/");
            if(exception != null)
                log.error(exception.getMessage());
            exception = Support.ExecuteCommand(zone, "/root/centos8_mysql.sh");
            if(exception != null)
                log.error(exception.getMessage());
        }
    }

    private void ChronyConfig(ArrayList<Zone> zones, String obcontrol) {
        for(int i = 0; i < zones.size(); i++) {
            Zone zone = zones.get(i);
            if(i == 0) {
                Exception exception = Support.SendFile(zone, "src/main/resources/oceanbase/obcontrol_chrony.sh", "/root/");
                if(exception != null)
                    log.error(exception.getMessage());
                exception = Support.ExecuteCommand(zone, "/root/obcontrol_chrony.sh");
                if(exception != null)
                    log.error(exception.getMessage());
            }
            else {
                Exception exception = Support.SendFile(zone, "src/main/resources/oceanbase/observer_chrony.sh", "/root/");
                if(exception != null)
                    log.error(exception.getMessage());
                exception = Support.ExecuteCommand(zone, "/root/observer_chrony.sh " + obcontrol);
                if(exception != null)
                    log.error(exception.getMessage());
            }
        }
    }

    private void SSH(Zone controlZone) {
        Exception exception = Support.SendFile(controlZone, "src/main/resources/ssh/scp.sh", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = Support.SendFile(controlZone, "src/main/resources/ssh/ssh.sh", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = Support.SendFile(controlZone, "src/main/resources/ssh/ssh_control.sh", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = Support.ExecuteCommand(controlZone, "/root/ssh_control.sh");
        if(exception != null)
            log.error(exception.getMessage());
    }

    private void OBD(Zone controlZone) {
        // TODO 根据obcontrol和observer修改配置文件 或者这个就直接丢给用户自己改去
        Exception exception = Support.SendFile(controlZone, "src/main/resources/oceanbase/mini-distributed-example.yaml", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = Support.SendFile(controlZone, "src/main/resources/oceanbase/obcontrol_obd.sh", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = Support.ExecuteCommand(controlZone, "/root/obcontrol_obd.sh");
        if(exception != null)
            log.error(exception.getMessage());
    }
}
