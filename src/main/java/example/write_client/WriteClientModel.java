package example.write_client;

import core.client.ClientInvokeResponse;
import core.model.Model;
import core.model.ModelStepResponse;

public class WriteClientModel implements Model {

    @Override
    public Object Init() {
        return 0;       // money的初始值
    }

    @Override
    public ModelStepResponse<?> Step(Object oldState, Object input, ClientInvokeResponse<?> output) {
        Integer oldMoney = (Integer) oldState;
        Integer updateValue = (Integer) input;
        if(!output.isSuccess())
            return new ModelStepResponse<>(true, oldMoney);
        boolean equal =  updateValue == output.getNewState();
        return new ModelStepResponse<>(equal, output.getNewState());
    }
}
