package core.checker.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
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
}
