package core.client;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JSONType(orders={"success", "newState"})
public class ClientInvokeResponse<T> {
    private boolean success;        // client.invoke是否成功
    private T newState;
}
