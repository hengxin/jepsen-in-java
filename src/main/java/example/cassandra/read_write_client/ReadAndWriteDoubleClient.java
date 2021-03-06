package example.cassandra.read_write_client;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import core.checker.checker.Operation;
import core.client.Client;
import core.client.ClientInvokeResponse;
import core.client.ClientRequest;
import core.db.Node;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;


@Slf4j
public class ReadAndWriteDoubleClient extends Client {

    Cluster cluster;
    Random random;

    public ReadAndWriteDoubleClient(int requestCount) {
        super.setRequestCount(requestCount);
        this.random = new Random();
    }

    @Override
    public Exception SetUp(Node node) {
        this.cluster = Cluster.builder().addContactPoint(node.getIp()).withPort(node.getPort()).build();
        Session session = this.cluster.connect();
        String initSQL1 = "CREATE KEYSPACE t WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};";
        String changeSQL = "USE t;";
        String initSQL2 = "CREATE TABLE rw(id int PRIMARY KEY, num int);";
        String initSQL3 = "INSERT INTO rw (id, num) VALUES(0, 0)";
        session.execute(initSQL1);
        session.execute(changeSQL);
        session.execute(initSQL2);
        session.execute(initSQL3);
        return null;
    }

    @Override
    public ClientRequest NextRequest() {
        float f = random.nextFloat();
        if(f <= 0.5)
            return new ClientRequest(Operation.F.READ, "num", 0);
        else {
            int nextValue = random.nextInt(100);
            return new ClientRequest(Operation.F.WRITE, "num", nextValue);
        }
    }

    @Override
    public ClientInvokeResponse<?> Invoke(ClientRequest request) {
        try {
            Session session = this.cluster.connect("t");
            if(request.getFunction() == Operation.F.READ) {
                String readSQL = "SELECT " + request.getTarget() + " from rw where id = 0;";
                ResultSet resultSet = session.execute(readSQL);
                return new ClientInvokeResponse<>(true, resultSet.one().getInt(0));
                // TODO resultset?????? ??????????????????????????????
            }
            else {
                String writeSQL = "UPDATE rw SET " + request.getTarget() + "=" + request.getValue() + " where id = 0;";
                session.execute(writeSQL);
                return new ClientInvokeResponse<>(true, request.getValue());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ClientInvokeResponse<>(false, 0);
        }
    }

    @Override
    public Exception TearDown() {
        String dropKeyspaceSQL = "Drop KEYSPACE t;";
        Session session = this.cluster.connect();
        session.execute(dropKeyspaceSQL);
        return null;
    }
}
