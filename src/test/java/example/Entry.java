package example;

import core.control.ControlConfig;
import core.control.Controller;
import core.db.Zone;
import core.nemesis.Nemesis;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Constant;
import util.Support;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Entry {

    Constant constant = new Constant();

    @BeforeEach
    public void init() {
        constant.Init();
    }

    @Test
    public void test() {
        ArrayList<Zone> zones = new ArrayList<>();
        zones.add(new Zone("192.168.62.7", 2881, "root", "root"));
        zones.add(new Zone("192.168.62.8", 2881, "root", "root"));
        zones.add(new Zone("192.168.62.9", 2881, "root", "root"));
        ControlConfig controlConfig = new ControlConfig("Oceanbase", zones, 3);
        Controller controller = new Controller(controlConfig, new WriteClientCreator(), constant.RANDOM_KILL);
        controller.Run();
    }


    @Test
    public void t() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String value = dateFormat.format(date);
        System.out.println(value);
    }
}
