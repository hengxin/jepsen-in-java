package example;

import core.control.ControlConfig;
import core.control.Controller;
import core.db.Zone;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Constant;
import java.util.ArrayList;


@Slf4j
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
    }
}
