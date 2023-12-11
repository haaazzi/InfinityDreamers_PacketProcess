package com.infinitydreamers.mqtt;

import org.json.JSONObject;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

public class SensorNode extends InputOutputNode {

    @Override
    public void process() {
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {

            Message message = getInputWire(0).get();

            if (message.isFlag()) {
                JSONObject data = new JSONObject(message.getJson().get("payload").toString());

                messageProcessing(data, message);
            }
        }
    }

    void messageProcessing(JSONObject data, Message message) {

        if (data.has("object")) {
            JSONObject object = (JSONObject) data.get("object");
            String commonTopic = message.getJson().getString("outputTopic");
            String sensor = message.getJson().has("sensor") ? message.getJson().getString("sensor") : null;
            JSONObject payload = null;
            Message newMessage = null;
            if (sensor != null) {
                for (String key : sensor.split(",")) {
                    newMessage = new Message();
                    payload = new JSONObject();

                    newMessage.put("outputTopic", commonTopic + "/e/" + key);
                    payload.put("time", System.currentTimeMillis());
                    payload.put("value", object.get(key));

                    newMessage.setFlag(true);
                    newMessage.put("payload", payload.toString());

                    output(newMessage);
                }
            } else {
                for (String key : object.keySet()) {
                    newMessage = new Message();
                    payload = new JSONObject();

                    newMessage.put("outputTopic", commonTopic + "/e/" + key);
                    payload.put("time", System.currentTimeMillis());
                    payload.put("value", object.get(key));

                    newMessage.setFlag(true);
                    newMessage.put("payload", payload.toString());

                    output(newMessage);
                }
            }
        } else {
            Message fail = new Message();
            fail.put("fail", "Sensor not found");
            fail.setFlag(false);
            output(fail);
        }
    }
}
