package com.infinitydreamers.modbus;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

public class ModbusMaster extends InputOutputNode {
    @Override
    public void process() {
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            Message message = getInputWire(0).get();

            if (message.isFlag()) {
                message.setFlag(true);
                output(message);
            }
        }
    }
}
