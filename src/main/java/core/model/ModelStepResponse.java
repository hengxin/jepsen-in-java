package core.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class ModelStepResponse<T> {

    private boolean success;        // 状态是否可以正确转化
    private T newState;

}
