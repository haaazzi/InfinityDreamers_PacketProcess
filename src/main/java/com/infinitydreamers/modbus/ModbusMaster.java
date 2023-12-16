package com.infinitydreamers.modbus;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

/**
 * Modbus 메시지를 처리하고 플래그를 설정하여 메시지를 출력하는 Node
 */
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
