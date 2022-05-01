package core.checker.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.common.value.qual.ArrayLen;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Object valid;
    private List<Map<String, Object>> exceptions;
    private Object error;
    Map<String, Result> results;
    List<List<Long>> reads;
    int count;
    List<Object> matches;
    Object finalQueue;
    Map<String,Object> res;

    public Result(Object valid){
        this.valid=valid;
    }
}
