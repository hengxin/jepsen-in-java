package example.bank;

import core.client.Client;
import core.client.ClientInvokeResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import util.Support;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;


@Slf4j
public class BankClient extends Client {

    // 主要还是nextRequest会用，真正转的时候还得先查询一下账户余额不然有可能这里的值是没来得及更新的
    ArrayList<Account> accounts;
    Random random;

    public BankClient(int requestCount, ArrayList<Account> accounts) {
        super.setRequestCount(requestCount);
        this.accounts = accounts;
        this.random = new Random();
    }

    @Override
    public Exception SetUp() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS bank (id int primary key, money int);";
        String initSQL1 = "INSERT INTO bank VALUES(0,200);";
        String initSQL2 = "INSERT INTO bank VALUES(1,300);";
        return Support.JDBCUpdate(this, createTableSQL + initSQL1 + initSQL2);
    }

    @Override
    public Object NextRequest() {
        float f = random.nextFloat();
        if(f <= 0.5) {
            int transferMoney = random.nextInt(this.accounts.get(0).getMoney());
            return new TransferRequest(0, 1, transferMoney);
        } else {
            int transferMoney = random.nextInt(this.accounts.get(1).getMoney());
            return new TransferRequest(1, 0, transferMoney);
        }
    }

    @Override
    public ClientInvokeResponse<?> Invoke(Object request) {
        TransferRequest transferRequest = (TransferRequest) request;
        int fromId = transferRequest.getFromId();
        int toId = transferRequest.getToId();
        int transferMoney = transferRequest.getTransferMoney();

        String selectSQLFormat = "SELECT money from bank where id = %s";
        Function<ResultSet, Integer> handle = (ResultSet rs) -> {
            try {
                rs.next();
                return rs.getInt("money");
            } catch (Exception e) {
                log.error(e.getMessage());
                return null;
            }
        };
        // check money before real transfer
        Integer fromMoneyInFact = Support.JDBCQueryWithClient(this, String.format(selectSQLFormat, fromId), handle);
        Integer toMoneyInFact = Support.JDBCQueryWithClient(this, String.format(selectSQLFormat, toId), handle);
        if(fromMoneyInFact == null || toMoneyInFact == null || fromMoneyInFact - transferMoney < 0)
            return new ClientInvokeResponse<>(false, this.accounts);

        int fromMoneyFinal = fromMoneyInFact - transferMoney;
        int toMoneyFinal = toMoneyInFact + transferMoney;
        String transferSQLFormat = "UPDATE bank SET money = %s where id = %s;";
        String transferSQL1 = String.format(transferSQLFormat, fromMoneyFinal, fromId);
        String transferSQL2 = String.format(transferSQLFormat, toMoneyFinal, toId);
        Exception exception = Support.JDBCUpdate(this, transferSQL1 + transferSQL2);
        if(exception == null){
            this.accounts.get(fromId).setMoney(fromMoneyFinal);
            this.accounts.get(toId).setMoney(toMoneyFinal);
            return new ClientInvokeResponse<>(true, this.accounts);
        }
        else return new ClientInvokeResponse<>(false, this.accounts);
    }

    @Override
    public Exception TearDown() {
        String dropSQL = "DROP TABLE bank;";
        return Support.JDBCUpdate(this, dropSQL);
    }

}

@Data
@AllArgsConstructor
class Account {
    private int id;
    private int money;
}

@Data
@AllArgsConstructor
class TransferRequest {
    private int fromId;
    private int toId;
    private int transferMoney;
}