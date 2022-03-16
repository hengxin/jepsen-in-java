package core.client;

import core.db.Zone;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

abstract public class Client {

    private Connection connection;

    abstract public Exception Start();

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}