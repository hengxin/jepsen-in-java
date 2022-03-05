package util;

import core.db.DB;
import core.db.NoopDB;
import core.nemesis.KillNemesis;
import core.nemesis.Nemesis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


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

    static public String TxtToString(String filePath) {
        try {
            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path);
            StringBuilder result  = new StringBuilder();
            for(String line: lines) {
                if(!line.equals(""))
                    result.append(line).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
