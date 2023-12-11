package com.infinitydreamers.mqtt;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

public class MqttOut extends InputOutputNode {

    @Override
    public void process() {
        String publisherId = UUID.randomUUID().toString();

        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            try (IMqttClient client = new MqttClient("tcp://localhost", publisherId)) {
                client.connect();
                Message message = getInputWire(0).get();
                if (message.hasJson() && message.isFlag() && message.getJson().has("outputTopic")) {
                    message.setFlag(true);
                    String topic = message.getJson().get("outputTopic").toString();
                    String data = message.getJson().get("payload").toString();
                    output(message);

                    client.publish(topic, new MqttMessage(data.getBytes()));

                    client.disconnect();
                } else {
                    Message fail = new Message();
                    fail.put("fail", "Out failed");
                    fail.setFlag(false);
                    output(fail);
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}