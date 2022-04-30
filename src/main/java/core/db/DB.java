package core.db;


import java.util.ArrayList;

public interface DB {
    // SetUp initializes the database.
    Exception SetUp(Node node);
    // TearDown tears down the database.
    Exception TearDown(Node node);
    // Name returns the unique name for the database.
    String Name();
    // Setup all needed config in db cluster
    Exception SetConfig(ArrayList<Node> nodes);
}