package core.model;

import core.client.ClientInvokeResponse;

public interface Model {

    // 初始化model的成员变量 state相关
    Object Init();

    // oldState + input =? output
    ModelStepResponse Step(Object oldState, Object input, ClientInvokeResponse<?> output);
}