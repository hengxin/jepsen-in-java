package core.db;


import java.util.HashMap;

public interface DB {

    HashMap<String, DB> DB_MAP = new HashMap<>();

    static void RegisterDB(DB db) {
        if(!DB_MAP.containsKey(db.Name()))
            DB_MAP.put(db.Name(), db);
        else
            System.out.println("Duplicate db key " + db.Name() + ", discarded!");
    }

    static DB GetDB(String name) {
        return DB_MAP.get(name);        // attention maybe null
    }

    // SetUp initializes the database.
    Exception SetUp(Zone zone);
    // TearDown tears down the database.
    Exception TearDown(Zone zone);
    // Name returns the unique name for the database.
    String Name();
}