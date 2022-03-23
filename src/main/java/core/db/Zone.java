package core.db;


public class Zone {
    private String ip;
    private int port;
    private String username;        // mysql的，租户用
    private String password;

    public Zone(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getOceanBaseURL(){
        return "jdbc:mysql://" + ip + ":" + port + "/oceanbase?autoReconnect=true";
//        return "jdbc:mysql://" + ip + ":" + port + "/oceanbase?connectTimeout=4000";
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
