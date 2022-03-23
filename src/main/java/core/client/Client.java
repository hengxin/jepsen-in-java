package core.client;

import java.sql.Connection;


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