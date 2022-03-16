package core.control;

import com.jcraft.jsch.Session;
import core.db.Zone;

import java.util.ArrayList;

public class ControlConfig {
    private String dbName;
    private ArrayList<Zone> zones;
    private int clientCount;

    public ControlConfig(String dbName, ArrayList<Zone> zones, int clientCount) {
        this.dbName = dbName;
        this.zones = zones;
        this.clientCount = clientCount;
    }

    public String getDBName() {
        return dbName;
    }

    public ArrayList<Zone> getZones() {
        return zones;
    }

    public int getClientCount() {
        return clientCount;
    }
}
