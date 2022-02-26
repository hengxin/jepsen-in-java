package util;

import core.db.DB;
import core.db.NoopDB;
import core.nemesis.KillNemesis;
import core.nemesis.Nemesis;


public class Constant {

    public static final int SSH_PORT = 22;
    public static final int OCEANBASE_PORT = 2881;

    public static final String WGET_EXEC = "yum -y install wget";

    static {
        // Register DB
        NoopDB noopDB = new NoopDB();

        DB.RegisterDB(noopDB);


        // Register Nemesis
        KillNemesis killNemesis = new KillNemesis();

        Nemesis.RegisterNemesis(killNemesis);
    }
}
