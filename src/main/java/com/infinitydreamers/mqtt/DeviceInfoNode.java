package com.infinitydreamers.mqtt;

import org.json.JSONObject;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

public class DeviceInfoNode extends InputOutputNode {

    @Override
    public void process() {
        String deviceInfoString = "deviceInfo";
        String objectString = "object";
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            Message message = getInputWire(0).get();
            if (message.isFlag()) {
                JSONObject json = new JSONObject(message.getJson().get("payload").toString());

                if (json.has(deviceInfoString) && json.has(objectString)) {
                    JSONObject newJson = new JSONObject();
                    JSONObject deviceInfo = (JSONObject) ((JSONObject) json.get(deviceInfoString)).get("tags");

                    deviceInfo.put("deviceId", ((JSONObject) json.get(deviceInfoString)).get("devEui"));
                    newJson.put(deviceInfoString, deviceInfo);
                    newJson.put(objectString, json.get(objectString));

                    message.put("payload", newJson.toString());
                    message.setFlag(true);
                    output(message);
                } else {
                    Message fail = new Message();
                    fail.put("fail", "DeviceInfo not found");
                    fail.setFlag(false);
                    output(fail);
                }
            }
        }
    }
}
