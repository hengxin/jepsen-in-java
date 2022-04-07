package example.bank;

import core.client.ClientInvokeResponse;
import core.model.Model;
import core.model.ModelStepResponse;

public class BankClientModel implements Model {
    @Override
    public Object Init() {
        return null;
    }

    @Override
    public ModelStepResponse Step(Object oldState, Object input, ClientInvokeResponse<?> output) {
        return null;
    }
}
