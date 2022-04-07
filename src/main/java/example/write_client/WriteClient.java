package example.write_client;

import core.client.Client;
import core.client.ClientInvokeResponse;
import lombok.extern.slf4j.Slf4j;
import util.Support;

import java.util.Random;


@Slf4j
public class WriteClient extends Client {

    int sequence;
    int money;
    Random random;

    public WriteClient(int sequence, int requestCount) {
        this.sequence = sequence;
        this.money = 0;
        super.setRequestCount(requestCount);
        random = new Random();
    }

    @Override
    public Exception SetUp() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS `account` (`nickname` VARCHAR(50) primary key, `money` int);";
        String initSQL = "INSERT INTO `account` VALUES('barney', 0);";          // 注意分号
        return Support.JDBCUpdate(this, createTableSQL + initSQL);
    }

    @Override
    public Object NextRequest() {
        return random.nextInt(100);
    }

    @Override
    public ClientInvokeResponse<?> Invoke(Object request) {
        Integer newMoney = (Integer) request;
        String writeSQL = String.format("UPDATE account SET money = %s where nickname = \"barney\";", newMoney);
        Exception exception = Support.JDBCUpdate(this, writeSQL);
        if(exception == null){
            this.money = newMoney;
            return new ClientInvokeResponse<>(true, this.money);
        }
        else return new ClientInvokeResponse<>(false, this.money);     // false的时候model.step一定是正确的，这里的值其实无所谓
    }

    @Override
    public Exception TearDown() {
        String dropSQL = "DROP TABLE account;";          // 注意分号
        return Support.JDBCUpdate(this, dropSQL);
    }

    public void log() {
        log.info("f");
    }
}