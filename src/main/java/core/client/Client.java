package core.client;

import core.db.Node;
import lombok.Data;


@Data
abstract public class Client {

    private int requestCount;       // 记得在创建client的时候填上这个值
    // TODO 也可以按时间来约束

    // TODO sql缓存 executeBatch 和 preparedSQL

    // 做一些准备工作，建表之类的
    abstract public Exception SetUp(Node node);
    // 下一个请求，用于记录
    abstract public ClientRequest NextRequest();
    // 实际执行语句
    abstract public ClientInvokeResponse<?> Invoke(ClientRequest request);
    // 把表删了
    abstract public Exception TearDown();

}