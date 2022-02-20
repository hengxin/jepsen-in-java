package core.control;

import core.db.Zone;

import java.util.ArrayList;

public class ControlConfig {
    private String db;
    private ArrayList<Zone> zones;
    private int clientCount;




    public String getDb() {
        return db;
    }

    public ArrayList<Zone> getZones() {
        return zones;
    }

    public int getClientCount() {
        return clientCount;
    }
}
