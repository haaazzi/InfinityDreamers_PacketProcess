package com.infinitydreamers.mqtt;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

public class MqttIn extends InputOutputNode {

    @Override
    public void process() {
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {

            Message message = getInputWire(0).get();

            if (message.isFlag()) {
                increaseSuccess();
            } else {
                increasefail();
            }
            String topic = message.getJson().get("topic").toString();
            if (topic.contains("device")) {
                output(message);
            } else {

            }
        }
    }
}
