package example;

import core.control.ControlConfig;
import core.control.Controller;
import core.db.Zone;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Constant;
import util.Support;
import java.sql.*;
import java.util.ArrayList;

import static util.Constant.*;


@Slf4j
public class Entry {

    Constant constant = new Constant();

    @BeforeEach
    public void init() {
        constant.Init();
    }

    @Test
    public void MainTest() {
        ArrayList<Zone> zones = new ArrayList<>();
        zones.add(new Zone("192.168.62.7", 2881, "root", "root"));
        zones.add(new Zone("192.168.62.8", 2881, "root", "root"));
        zones.add(new Zone("192.168.62.9", 2881, "root", "root"));
        ControlConfig controlConfig = new ControlConfig("Oceanbase", zones, 3);
        Controller controller = new Controller(controlConfig, new WriteClientCreator(), NEMESIS_GENERATOR_RANDOM_KILL);
//        Controller controller = new Controller(controlConfig, new WriteClientCreator(), NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION);
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
        String command = "systemctl restart chronyd.service && chronyc tracking";
//        String command = Constant.TxtToString("src/main/resources/centos8_mysql.txt");
//        String command = "timedatectl set-ntp true\n" +
//                "chronyc tracking";
//        String command = "chronyc tracking && chronyc sources -v";
//        String command = "systemctl status chronyd";
//        String command = "iptables -D INPUT 1\niptables -D INPUT 1";
//        String command = "iptables -D INPUT 1";
//        String command = "iptables -I INPUT -s 192.168.62.7 -j DROP\n" +
//                "iptables -I INPUT -s 192.168.62.9 -j DROP";
        for(String host: observers) {
            try {
                Support.ExecuteCommand(new Zone(host, 2881, "root", "root"), command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void JDBC() {
        try {
            String host = "192.168.62.8";
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + 2881 + "/oceanbase", "root", "root");
            Statement statement = connection.createStatement();
//            String sql = "select * from t;";
            String sql = "select svr_ip from gv$partition where role = 1 limit 1";      // role=1为主副本，role=2为从副本，这样看不同情况网络分区
            ResultSet rs = statement.executeQuery(sql);      // executeQuery用于select，executeUpdate用于create, insert, delete, update

            rs.next();
            String ip = rs.getString("svr_ip");
            System.out.println(ip);


            rs.close();
            statement.close();
            connection.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestShuffle() {
        int n = 3;
        ArrayList<Integer> arrayList = Support.ShuffleByCount(n);
        for(Integer integer: arrayList)
            System.out.print(integer + " ");
        System.out.println();
    }
}
