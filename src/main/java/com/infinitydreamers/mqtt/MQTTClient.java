package com.infinitydreamers.mqtt;

import java.util.UUID;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

/**
 * MQTT 통신을 담당하는 클라이언트 클래스
 */
public class MQTTClient extends InputOutputNode {
    Message message;
    static final String DEFAULT_TOPIC = "#";
    static final String DEFAULT_URI = "tcp://ems.nhnacademy.com:1883";
    IMqttClient client;

    public MQTTClient(Message message) {
        this.message = message;
    }

    @Override
    public void preprocess() {
        String publisherId = UUID.randomUUID().toString();
        String uri = message.getJson().has("uri") ? message.getJson().getString("uri") : DEFAULT_URI;

        try {
            client = new MqttClient(uri, publisherId);
            client.connect();
            String topicFilter = message.getJson().has("inputTopic") ? message.getJson().getString("inputTopic")
                    : DEFAULT_TOPIC;
            String sensor = message.getJson().has("sensor") ? message.getJson().getString("sensor") : null;

            client.subscribe(topicFilter, (topic, msg) -> {
                message.setFlag(true);
                message.setSensor(sensor);
                message.put("topic", topic);
                message.put("payload", msg.toString());
                output(message);
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postprocess() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
