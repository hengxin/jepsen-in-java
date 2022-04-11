package core.control;

import core.db.Zone;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;


@Data
@AllArgsConstructor
public class ControlConfig {
    private String dbName;
    private ArrayList<Zone> zones;
    private int clientCount;
}
