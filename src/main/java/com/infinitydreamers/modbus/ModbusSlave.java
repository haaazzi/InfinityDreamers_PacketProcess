package com.infinitydreamers.modbus;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

/**
 * 메시지를 처리하고 플래그를 설정하는 Modbus 슬레이브 Node
 */
public class ModbusSlave extends InputOutputNode {
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
