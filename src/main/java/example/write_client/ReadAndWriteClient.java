package example.write_client;

import core.client.Client;
import core.client.ClientInvokeResponse;
import lombok.extern.slf4j.Slf4j;
import util.Support;

import java.sql.ResultSet;
import java.util.Random;
import java.util.function.Function;


@Slf4j
public class ReadAndWriteClient extends Client {

    Random random;

    public ReadAndWriteClient(int requestCount) {
        super.setRequestCount(requestCount);
        random = new Random();
    }

    @Override
    public Exception SetUp() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS t (`name` VARCHAR(50) primary key, value int);";
        String initSQL = "INSERT INTO t VALUES('x', 0);";          // 注意分号
        return Support.JDBCUpdate(this, createTableSQL + initSQL);
    }

    @Override
    public Object NextRequest() {
        float f = random.nextFloat();
        if(f <= 0.5) {
            return new RWRequest("read", "x", 0);
        } else {
            int nextValue = random.nextInt(100);
            return new RWRequest("write", "x", nextValue);
        }
    }

    @Override
    public ClientInvokeResponse<?> Invoke(Object request) {
        RWRequest rwRequest = (RWRequest) request;
        if(rwRequest.getAction().equals("read")) {
            String readSQL = "SELECT value FROM t WHERE `name` = \"%s\";";
            Function<ResultSet, Integer> handle = (ResultSet rs) -> {
                try {
                    rs.next();
                    return rs.getInt("value");
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return null;
                }
            };
            Integer value = Support.JDBCQueryWithClient(this, String.format(readSQL, rwRequest.getName()), handle);
            if(value == null)
                return new ClientInvokeResponse<>(false, 0);
            return new ClientInvokeResponse<>(true, value);
        }
        else {
            String writeSQL = "UPDATE t SET value = %s where `name` = \"%s\";";
            Exception exception = Support.JDBCUpdate(this, String.format(writeSQL, rwRequest.getValue(), rwRequest.getName()));
            if(exception == null)
                return new ClientInvokeResponse<>(true, rwRequest.value);
            else return new ClientInvokeResponse<>(false, 0);     // false的时候model.step一定是正确的，这里的值其实无所谓
        }

    }

    @Override
    public Exception TearDown() {
        String dropSQL = "DROP TABLE t;";          // 注意分号
        return Support.JDBCUpdate(this, dropSQL);
    }
}