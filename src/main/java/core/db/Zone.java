package core.db;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Zone {
    private String ip;
    private int port;
    private String username;        // mysql的，租户用
    private String password;


    public String getOceanBaseURL(){
        return "jdbc:mysql://" + ip + ":" + port + "/oceanbase?autoReconnect=true";
//        return "jdbc:mysql://" + ip + ":" + port + "/oceanbase?connectTimeout=4000";
    }
}
