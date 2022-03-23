package example;

import core.client.Client;
import lombok.extern.slf4j.Slf4j;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;


@Slf4j
public class WriteClient extends Client {

    int sequence;

    public WriteClient(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public Exception Start() {
        Statement statement = null;
        try {
            statement = this.getConnection().createStatement();
            String createTableSQL = "CREATE TABLE IF NOT EXISTS t1 (c1 VARCHAR(50) primary key);";
            statement.executeUpdate(createTableSQL);
            for(int i = 0; i < 300; i++){
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String value = "IP." + sequence + " " + dateFormat.format(date);
                String writeSQL = String.format("INSERT INTO t1 VALUES(\"%s\");", value);
                statement.executeUpdate(writeSQL);
                // TODO 使用support通用函数
                // 这里如果不设置过期时间，节点数据库进程被stop 是会一直卡住直到进程恢复
                log.info("Successfully add " + value);
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return e;
        } finally {
            try {
                this.getConnection().close();
                if(statement != null)
                    statement.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
