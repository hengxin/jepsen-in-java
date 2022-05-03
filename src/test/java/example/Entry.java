package example;

import core.checker.checker.Linearizable;
import core.checker.checker.Operation;
import core.checker.model.Register;
import core.checker.vo.Result;
import core.control.ControlConfig;
import core.control.Controller;
import core.db.Node;
import core.record.Recorder;
import example.cassandra.read_write_client.ReadAndWriteDoubleClientCreator;
import example.oceanbase.read_write_client.ReadAndWriteClientCreator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import util.Constant;
import util.Support;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static util.Constant.NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION;


@Slf4j
public class Entry {

    Constant constant = new Constant();

    @Test
    public void CassandraRWTest() {
        // cassandra在网络分区后 依旧可以成功写入 不会受到影响 但最终结果是非线性一致性的
        // 但如果不引入nemesis 是可以通过check的 因此说明cassandra在故障注入方面做的不好
        constant.Init();
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(new Node("192.168.62.7", 9042, "root", "root"));
        nodes.add(new Node("192.168.62.8", 9042, "root", "root"));
        nodes.add(new Node("192.168.62.9", 9042, "root", "root"));
        Recorder recorder = new Recorder("output/cassandra/read_write_client/", "bad.txt");
        ControlConfig controlConfig = new ControlConfig("Cassandra", nodes, 3);
//        Controller controller = new Controller(controlConfig, new WriteClientCreator(), NEMESIS_GENERATOR_RANDOM_KILL);
        Controller controller = new Controller(controlConfig, new ReadAndWriteDoubleClientCreator(), NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION, recorder,
                                "wgl", new Register(0));
//        Controller controller = new Controller(controlConfig, new WriteClientCreator(), NEMESIS_GENERATOR_ASYMMETRIC_NETWORK_PARTITION);
        controller.Run();
    }

    @Test
    public void OceanBaseRWTest() {
        constant.Init();
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(new Node("192.168.62.7", 2881, "root", "root"));
        nodes.add(new Node("192.168.62.8", 2881, "root", "root"));
        nodes.add(new Node("192.168.62.9", 2881, "root", "root"));
        Recorder recorder = new Recorder("output/oceanbase/read_write_client/", "good.txt");
        ControlConfig controlConfig = new ControlConfig("Oceanbase", nodes, 3);
//        Controller controller = new Controller(controlConfig, new WriteClientCreator(), NEMESIS_GENERATOR_RANDOM_KILL);
        Controller controller = new Controller(controlConfig, new ReadAndWriteClientCreator(), NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION, recorder,
                                "wgl", new Register(0));
//        Controller controller = new Controller(controlConfig, new WriteClientCreator(), NEMESIS_GENERATOR_ASYMMETRIC_NETWORK_PARTITION);
        controller.Run();
    }

    @Test
    public void ExecSeparately() {
        String[] hosts = {"192.168.62.6", "192.168.62.7", "192.168.62.8", "192.168.62.9"};
        String[] obcontrol = {"192.168.62.6"};
        String[] observers = {"192.168.62.7", "192.168.62.8", "192.168.62.9"};
        String[] test_server = {"192.168.62.9"};
//        String command = "systemctl status firewalld.service";
//        String command = "systemctl restart chronyd.service && chronyc tracking";
//        String command = Constant.TxtToString("src/main/resources/centos8_mysql.sh");
//        String command = "timedatectl set-ntp true\n" +
//                "chronyc tracking";
//        String command = "chronyc tracking && chronyc sources -v";
//        String command = "systemctl status chronyd";
        String command = "iptables -D INPUT 1\niptables -D INPUT 1";
//        String command = "iptables -D INPUT 1";
//        String command = "iptables -I INPUT -s 192.168.62.7 -j DROP\n" +
//                "iptables -I INPUT -s 192.168.62.8 -j DROP";
//        String command = "rm -rf /var/lib/cassandra/data\nsystemctl restart cassandra";
//        String command = "systemctl start cassandra";
//        String command = "systemctl stop cassandra";
        for(String host: test_server) {
            try {
                Support.ExecuteCommand(new Node(host, 2881, "root", "root"), command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void Test() {
        // 还有个bad 是因为一开始的值未变0 所以一开始读出来是88是上个测试遗留下的值

        ArrayList<Operation> operations = Support.TxtToOperations("output/cassandra/read_write_client/good.txt");
        Result result = new Linearizable(new HashMap(Map.of("algorithm", "wgl", "model", new Register(0))))
                .check(new HashMap(Map.of("name", "cassandra", "start-time", LocalDateTime.now())), operations, new HashMap<>());
        System.out.println(result.getValid());
    }
}
