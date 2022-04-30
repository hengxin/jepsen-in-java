package core.db;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

import static util.Support.*;


@Slf4j
public class OceanbaseDB implements DB {
    @Override
    public Exception SetUp(Node node) {
        log.info("Set up OceanbaseDB in" + node.getIp());
        // Oceanbase使用obdeploy统一安装配置，所以这里不需要每个节点再单独下载了
        return null;
    }

    @Override
    public Exception TearDown(Node node) {
        return null;
    }

    @Override
    public String Name() {
        return "Oceanbase";
    }

    @Override
    public Exception SetConfig(ArrayList<Node> nodes) {         // TODO 这里还没整体测 但用的都是之前可以用的东西
        if(nodes.size() == 0)
            return new Exception("Nodes is empty");
        String obcontrol = nodes.get(0).getIp();            // 默认第一个节点为obcontrol
        StringBuilder observerBuilder = new StringBuilder();
        for(Node node : nodes)
            observerBuilder.append(node.getIp()).append(" ");
        String observer = observerBuilder.toString().strip();
        log.info("Set config for OceanbaseDB to all nodes");
        log.info("OBcontrol is " + obcontrol);
        log.info("OBserver is " + observer);

        BasicConfig(nodes, obcontrol, observer);
        ChronyConfig(nodes, obcontrol);
        SSH(nodes.get(0));
        OBD(nodes.get(0));
        return null;
    }

    private void BasicConfig(ArrayList<Node> nodes, String obcontrol, String observer) {
        for(Node node : nodes) {
            Exception exception = SendFile(node, "src/main/resources/oceanbase/obconfig.sh", "/root/");
            if(exception != null)
                log.error(exception.getMessage());
            exception = ExecuteCommand(node, ShellCommand("/root/obconfig.sh", obcontrol + " " + observer));
            if(exception != null)
                log.error(exception.getMessage());


            // download mysql for tenant
            exception = SendFile(node, "src/main/resources/mysql/mysql-community.repo", "/etc/yum.repos.d/");
            if(exception != null)
                log.error(exception.getMessage());
            exception = SendFile(node, "src/main/resources/mysql/centos8_mysql.sh", "/root/");
            if(exception != null)
                log.error(exception.getMessage());
            exception = ExecuteCommand(node, ShellCommand("/root/centos8_mysql.sh", ""));
            if(exception != null)
                log.error(exception.getMessage());
        }
    }

    private void ChronyConfig(ArrayList<Node> nodes, String obcontrol) {
        for(int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if(i == 0) {
                Exception exception = SendFile(node, "src/main/resources/oceanbase/obcontrol_chrony.sh", "/root/");
                if(exception != null)
                    log.error(exception.getMessage());
                exception = ExecuteCommand(node, ShellCommand("/root/obcontrol_chrony.sh", ""));
                if(exception != null)
                    log.error(exception.getMessage());
            }
            else {
                Exception exception = SendFile(node, "src/main/resources/oceanbase/observer_chrony.sh", "/root/");
                if(exception != null)
                    log.error(exception.getMessage());
                exception = ExecuteCommand(node, ShellCommand("/root/observer_chrony.sh", obcontrol));
                if(exception != null)
                    log.error(exception.getMessage());
            }
        }
    }

    private void SSH(Node controlNode) {
        Exception exception = SendFile(controlNode, "src/main/resources/ssh/scp.sh", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = SendFile(controlNode, "src/main/resources/ssh/ssh.sh", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = SendFile(controlNode, "src/main/resources/ssh/ssh_control.sh", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = ExecuteCommand(controlNode, ShellCommand("/root/ssh_control.sh", ""));
        if(exception != null)
            log.error(exception.getMessage());
    }

    private void OBD(Node controlNode) {
        // TODO 根据obcontrol和observer修改配置文件 或者这个就直接丢给用户自己改去
        Exception exception = SendFile(controlNode, "src/main/resources/oceanbase/mini-distributed-example.yaml", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = SendFile(controlNode, "src/main/resources/oceanbase/obcontrol_obd.sh", "/root/");
        if(exception != null)
            log.error(exception.getMessage());
        exception = ExecuteCommand(controlNode, ShellCommand("/root/obcontrol_obd.sh", ""));
        if(exception != null)
            log.error(exception.getMessage());
    }
}
