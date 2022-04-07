package core.record;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Date;


@Data
@AllArgsConstructor
@ToString
@JSONType(orders={"requestId", "threadId", "action", "time", "data"})
public class Operation<T> {

    // requestId + threadId 可以确定一对invoke和response
    private Integer requestId;      // start with 0
    private Integer threadId;
    private ActionEnum action;
    private Date time;
    private T data;

}