package core.db;


public interface DB {
    // SetUp initializes the database.
    Exception SetUp(Zone zone);
    // TearDown tears down the database.
    Exception TearDown(Zone zone);
    // Name returns the unique name for the database.
    String Name();
}