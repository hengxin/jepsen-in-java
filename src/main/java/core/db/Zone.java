package core.db;

import java.sql.Connection;

public class Zone {
    private String ip;
    private int port;
    private String name;        // Zone本身自己的名字
    private String username;
    private String password;

    public String getURL(){
        return "jdbc:mysql://" + ip + ":" + port + "/oceanbase";
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
