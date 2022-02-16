package core.client;

import core.db.Zone;

import java.sql.Connection;
import java.sql.DriverManager;

public class NoopClient implements Client {

    private Connection dbConnection;

    @Override
    public Exception SetUp(Zone zone) {
        try {
            dbConnection = DriverManager.getConnection(zone.getURL(), zone.getUsername(), zone.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
        return null;
    }

    @Override
    public Exception TearDown(Zone zone) {
        try {
            dbConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
        return null;
    }
}
