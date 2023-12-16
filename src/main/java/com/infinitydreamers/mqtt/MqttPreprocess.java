package com.infinitydreamers.mqtt;

import org.json.JSONException;
import org.json.JSONObject;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

import lombok.extern.slf4j.Slf4j;

/**
 * MqttPreprocess 클래스는 MQTT 메시지에 대한 전처리 작업을 수행하는 Node
 */
@Slf4j
public class MqttPreprocess extends InputOutputNode {

    @Override
    public void process() {
        String deviceInfoString = "deviceInfo";
        String objectString = "object";
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            Message message = getInputWire(0).get();

            if (message.isFlag()) {
                JSONObject json = null;
                try {
                    json = new JSONObject(message.getJson().getString("payload"));
                } catch (JSONException e) {
                    log.error(e.getMessage());
                }
                if (json != null && json.has(deviceInfoString) && json.has(objectString)) {
                    JSONObject newJson = new JSONObject();
                    JSONObject deviceInfo = (JSONObject) ((JSONObject) json.get(deviceInfoString)).get("tags");
                    JSONObject object = (JSONObject) json.get("object");

                    if (deviceInfo.length() >= 3) {
                        for (String key : object.keySet()) {
                            newJson.put("key", ((JSONObject) json.get(deviceInfoString)).get("devEui") + "-" + key);
                            newJson.put("value", object.get(key).toString());

                            Message newMessage = new Message(newJson);
                            newMessage.setFlag(true);
                            output(newMessage);
                        }
                    }
                } else {
                    increasefail();
                }
            }
        }
    }
}
