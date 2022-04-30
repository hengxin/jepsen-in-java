package core.client;

import core.checker.checker.Operation.F;
import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class ClientRequest {
    F function;
    Object target;
    Object value;
    // 按照需要可以继承此类添加新的成员变量
}
