package com.infinitydreamers.modbus;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

public class ModbusSlave extends InputOutputNode {
    @Override
    public void process() {
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            Message message = getInputWire(0).get();

            if (message.isFlag()) {
                // System.out.println(message.getJson().toString(4));
                message.setFlag(true);
                output(message);
            }
        }
    }
}
