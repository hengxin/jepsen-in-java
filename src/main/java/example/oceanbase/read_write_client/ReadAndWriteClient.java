package example.oceanbase.read_write_client;

import core.checker.checker.Operation;
import core.client.Client;
import core.client.ClientInvokeResponse;
import core.client.ClientRequest;
import core.db.Node;
import lombok.extern.slf4j.Slf4j;
import util.Support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Random;
import java.util.function.Function;


@Slf4j
public class ReadAndWriteClient extends Client {

    public Connection connection;
    private Random random;

    public ReadAndWriteClient(int requestCount) {
        super.setRequestCount(requestCount);
        random = new Random();
    }

    @Override
    public Exception SetUp(Node node) {
        String oceanBaseURL = "jdbc:mysql://" + node.getIp() + ":" + node.getPort() + "/oceanbase?autoReconnect=true";
        try {
            this.connection = DriverManager.getConnection(oceanBaseURL, node.getUsername(), node.getPassword());
            String createTableSQL = "CREATE TABLE IF NOT EXISTS t (`name` VARCHAR(50) primary key, value int);";
            String initSQL = "INSERT INTO t VALUES('x', 0);";          // 注意分号
            return Support.JDBCUpdate(this.connection, createTableSQL + initSQL);
        } catch (Exception e) {
            log.error(e.getMessage());
            return e;
        }
    }

    @Override
    public ClientRequest NextRequest() {
        float f = random.nextFloat();
        if(f <= 0.5) {
            return new ClientRequest(Operation.F.READ, "x", 0);
        } else {
            int nextValue = random.nextInt(100);
            return new ClientRequest(Operation.F.WRITE, "x", nextValue);
        }
    }

    @Override
    public ClientInvokeResponse<?> Invoke(ClientRequest request) {
        if(request.getFunction() == Operation.F.READ) {
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
            Integer value = Support.JDBCQueryWithClient(this.connection, String.format(readSQL, request.getTarget()), handle);
            if(value == null)
                return new ClientInvokeResponse<>(false, 0);
            return new ClientInvokeResponse<>(true, value);
        }
        else {
            String writeSQL = "UPDATE t SET value = %s where `name` = \"%s\";";
            Exception exception = Support.JDBCUpdate(this.connection, String.format(writeSQL, request.getValue(), request.getTarget()));
            if(exception == null)
                return new ClientInvokeResponse<>(true, request.getValue());
            else return new ClientInvokeResponse<>(false, 0);     // false的时候model.step一定是正确的，这里的值其实无所谓
        }

    }

    @Override
    public Exception TearDown() {
        String dropSQL = "DROP TABLE t;";          // 注意分号
        try {
            Support.JDBCUpdate(this.connection, dropSQL);
            this.connection.close();
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return e;
        }
    }
}