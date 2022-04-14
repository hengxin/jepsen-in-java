package example;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import core.client.ClientInvokeResponse;
import core.control.ControlConfig;
import core.control.Controller;
import core.db.Cassandra;
import core.db.Zone;
import core.model.ModelStepResponse;
import core.record.Operation;
import core.record.Recorder;
import example.bank.BankClientCreator;
import example.write_client.RWRequest;
import example.write_client.ReadAndWriteClientCreator;
import example.write_client.ReadAndWriteClientModel;
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
    public void BankClientMainTest() {
        constant.Init();
        ArrayList<Zone> zones = new ArrayList<>();
        zones.add(new Zone("192.168.62.7", 2881, "root", "root"));
        zones.add(new Zone("192.168.62.8", 2881, "root", "root"));
        zones.add(new Zone("192.168.62.9", 2881, "root", "root"));
        Recorder recorder = new Recorder("output/bank_client/", "history1.txt");
        ControlConfig controlConfig = new ControlConfig("Oceanbase", zones, 3);
//        Controller controller = new Controller(controlConfig, new BankClientCreator(), NEMESIS_GENERATOR_RANDOM_KILL);
        Controller controller = new Controller(controlConfig, new BankClientCreator(), NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION, recorder);
//        Controller controller = new Controller(controlConfig, new BankClientCreator(), NEMESIS_GENERATOR_ASYMMETRIC_NETWORK_PARTITION);
        controller.Run();
    }

    @Test
    public void ReadAndWriteClientMainTest() {
        constant.Init();
        ArrayList<Zone> zones = new ArrayList<>();
        zones.add(new Zone("192.168.62.7", 2881, "root", "root"));
        zones.add(new Zone("192.168.62.8", 2881, "root", "root"));
        zones.add(new Zone("192.168.62.9", 2881, "root", "root"));
        Recorder recorder = new Recorder("output/read_write_client/", "history1.txt");
        ControlConfig controlConfig = new ControlConfig("Oceanbase", zones, 3);
//        Controller controller = new Controller(controlConfig, new WriteClientCreator(), NEMESIS_GENERATOR_RANDOM_KILL);
        Controller controller = new Controller(controlConfig, new ReadAndWriteClientCreator(), NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION, recorder);
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
        String command = "rm -rf /var/lib/cassandra/data\nsystemctl restart cassandra";
        for(String host: observers) {
            try {
                Support.ExecuteCommand(new Zone(host, 2881, "root", "root"), command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void TestCheck() {
        ArrayList<Operation> operations = Support.TxtToOperations("output/read_write_client/history1.txt", RWRequest.class);
        ReadAndWriteClientModel model = new ReadAndWriteClientModel();
        ModelStepResponse<?> response = model.Step(0, operations.get(1).getData(), (ClientInvokeResponse<?>) operations.get(3).getData());
        System.out.println(response.toString());
    }

    @Test
    public void Test() {
        ArrayList<Zone> zones = new ArrayList<>();
        zones.add(new Zone("192.168.62.7", 2881, "root", "root"));
        zones.add(new Zone("192.168.62.8", 2881, "root", "root"));
        zones.add(new Zone("192.168.62.9", 2881, "root", "root"));
        Cassandra cassandra = new Cassandra();
//        for(Zone zone: zones)
//            cassandra.SetUp(zone);
        cassandra.SetConfig(zones);
    }

    @Test
    public void CassandraDemo() {
        Cluster cluster = Cluster.builder().addContactPoint("192.168.62.7").withPort(9042).build();
        Session session = cluster.connect();
        ResultSet rs = session.execute("select release_version from system.local");
        Row row = rs.one();
        System.out.println("RELEASE VERSION: " + row.getString("release_version"));
        session.close();
        cluster.close();
    }
}
