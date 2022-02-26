package core.control;

import core.db.Zone;

import java.util.ArrayList;

public class ControlConfig {
    private String dbName;
    private ArrayList<Zone> zones;
    private int clientCount;




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
