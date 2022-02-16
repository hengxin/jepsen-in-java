package core.client;

import core.db.Zone;

import java.sql.Connection;
import java.sql.DriverManager;

public interface Client {

    Exception SetUp(Zone zone);

    Exception TearDown(Zone zone);
}