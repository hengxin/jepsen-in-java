package example.oceanbase.read_write_client;

import core.client.ClientInvokeResponse;
import core.model.Model;
import core.model.ModelStepResponse;

public class ReadAndWriteClientModel implements Model {

    @Override
    public Object Init() {
        return 0;       // money的初始值
    }

    @Override
    public ModelStepResponse<?> Step(Object oldState, Object input, ClientInvokeResponse<?> output) {
        Integer x = (Integer) oldState;
        RWRequest rwRequest = (RWRequest) input;
        if(!output.isSuccess())
            return new ModelStepResponse<>(true, x);
        if(rwRequest.getAction().equals("read")) {
            boolean equal = x.equals(output.getNewState());
            return new ModelStepResponse<>(equal, output.getNewState());
        }
        else {
            return new ModelStepResponse<>(true, output.getNewState());
        }
    }
}
