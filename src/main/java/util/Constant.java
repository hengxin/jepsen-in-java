package util;

import com.jcraft.jsch.*;
import core.db.DB;
import core.db.OceanbaseDB;
import core.db.Zone;
import core.nemesis.KillNemesis;
import core.nemesis.Nemesis;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;


public class Constant {

    public static final int SSH_PORT = 22;

    public static final String RANDOM_KILL = "random_kill";

    public static HashMap<String, DB> DB_MAP = new HashMap<>();
    public static HashMap<String, Nemesis> NEMESIS_MAP = new HashMap<>();


    public void Init() {
        // Register DB
        OceanbaseDB oceanbaseDB = new OceanbaseDB();
        this.RegisterDB(oceanbaseDB);


        // Register Nemesis
        KillNemesis killNemesis = new KillNemesis();
        this.RegisterNemesis(killNemesis);
    }

    public void RegisterDB(DB db) {
        if(!DB_MAP.containsKey(db.Name()))
            DB_MAP.put(db.Name(), db);
        else
            System.out.println("Duplicate db key " + db.Name() + ", discarded!");
    }

    public void RegisterNemesis(Nemesis nemesis) {
        if(!NEMESIS_MAP.containsKey(nemesis.Name()))
            NEMESIS_MAP.put(nemesis.Name(), nemesis);
        else
            System.out.println("Duplicate nemesis key " + nemesis.Name() + ", discarded!");
    }

    public static DB GetDB(String name) {
        return DB_MAP.get(name);        // attention maybe null
    }

    public static Nemesis GetNemesis(String name) {
        return NEMESIS_MAP.get(name);           // attention maybe null
    }

}
