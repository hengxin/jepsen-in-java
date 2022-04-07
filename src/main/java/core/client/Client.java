package core.client;

import lombok.Data;

import java.sql.Connection;


@Data
abstract public class Client {

    private Connection connection;
    private int requestCount;
    // TODO 也可以按时间来约束

    // TODO sql缓存 executeBatch 和 preparedSQL

    // 做一些准备工作，建表之类的
    abstract public Exception SetUp();
    // 下一个请求，用于记录
    abstract public Object NextRequest();
    // 实际执行语句
    abstract public ClientInvokeResponse<?> Invoke(Object request);
    // 把表删了
    abstract public Exception TearDown();

}