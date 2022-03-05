package core.client;

import core.db.Zone;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

abstract public class Client {

    public Statement statement;

    abstract public Exception Start();
}