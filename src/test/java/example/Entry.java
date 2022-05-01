package example;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import core.checker.model.Register;
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

import java.util.ArrayList;

import static util.Constant.NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION;


@Slf4j
public class Entry {

    Constant constant = new Constant();

    @Test
    public void CassandraRWTest() {
        constant.Init();
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(new Node("192.168.62.7", 9042, "root", "root"));
        nodes.add(new Node("192.168.62.8", 9042, "root", "root"));
        nodes.add(new Node("192.168.62.9", 9042, "root", "root"));
        Recorder recorder = new Recorder("output/cassandra/read_write_client/", "history1.txt");
        ControlConfig controlConfig = new ControlConfig("Cassandra", nodes, 3);
//        Controller controller = new Controller(controlConfig, new WriteClientCreator(), NEMESIS_GENERATOR_RANDOM_KILL);
        Controller controller = new Controller(controlConfig, new ReadAndWriteDoubleClientCreator(), NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION, recorder,
                                "wgl", new Register(0));
//        Controller controller = new Controller(controlConfig, new WriteClientCreator(), NEMESIS_GENERATOR_ASYMMETRIC_NETWORK_PARTITION);
        controller.Run();
    }

    @Test
    public void ReadAndWriteClientMainTest() {
        constant.Init();
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(new Node("192.168.62.7", 2881, "root", "root"));
        nodes.add(new Node("192.168.62.8", 2881, "root", "root"));
        nodes.add(new Node("192.168.62.9", 2881, "root", "root"));
        Recorder recorder = new Recorder("output/oceanbase/read_write_client/", "history1.txt");
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
        String[] test_server = {"192.168.62.8"};
//        String command = "systemctl status firewalld.service";
//        String command = "systemctl restart chronyd.service && chronyc tracking";
//        String command = Constant.TxtToString("src/main/resources/centos8_mysql.sh");
//        String command = "timedatectl set-ntp true\n" +
//                "chronyc tracking";
//        String command = "chronyc tracking && chronyc sources -v";
//        String command = "systemctl status chronyd";
//        String command = "iptables -D INPUT 1\niptables -D INPUT 1";
//        String command = "iptables -D INPUT 1";
//        String command = "iptables -I INPUT -s 192.168.62.7 -j DROP\n" +
//                "iptables -I INPUT -s 192.168.62.9 -j DROP";
//        String command = "rm -rf /var/lib/cassandra/data\nsystemctl restart cassandra";
        String command = "systemctl start cassandra";
//        String command = "systemctl stop cassandra";
        for(String host: observers) {
            try {
                Support.ExecuteCommand(new Node(host, 2881, "root", "root"), command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void Test() {
        String s = "" + 1 + 103;
        System.out.println(s);
    }

    @Test
    public void CassandraDemo() {
        Cluster cluster = Cluster.builder().addContactPoint("192.168.62.7").withPort(9042).build();
        Session session = cluster.connect("t");
        ResultSet rs = session.execute("select num1 from rw where id=0;");
        Row row = rs.one();
        System.out.println(row.getInt(0));
        session.close();
        cluster.close();
    }
}
