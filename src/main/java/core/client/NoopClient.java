package core.client;

import core.db.Zone;

import java.sql.Connection;
import java.sql.DriverManager;

public class NoopClient extends Client {
    @Override
    public Exception Start() {
        return null;
    }
}
