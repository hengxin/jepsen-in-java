package util;

import core.db.DB;
import core.db.NoopDB;
import core.nemesis.KillNemesis;
import core.nemesis.Nemesis;


public class Constant {

    static {
        // Register DB
        NoopDB noopDB = new NoopDB();

        DB.RegisterDB(noopDB);


        // Register Nemesis
        KillNemesis killNemesis = new KillNemesis();

        Nemesis.RegisterNemesis(killNemesis);
    }
}
