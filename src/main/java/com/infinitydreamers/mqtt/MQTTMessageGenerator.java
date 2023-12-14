package com.infinitydreamers.mqtt;

import org.json.JSONObject;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

public class MQTTMessageGenerator extends InputOutputNode {
    @Override
    public void process() {
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            Message message = getInputWire(0).get();
            // s/nhn/b/gy/p/class_a/e/hum
            if (message.isFlag()) {
                JSONObject object = message.getJson();
                if (object.has("place")) {
                    String[] place = object.getString("place").split("/");

                    StringBuilder topicBuilder = new StringBuilder();
                    topicBuilder.append("s/" + place[0]);
                    topicBuilder.append("/b/" + place[1]);
                    topicBuilder.append("/p/" + place[2]);
                    topicBuilder.append("/e/" + object.getString("key").split("-")[1]);

                    JSONObject payload = new JSONObject();
                    payload.put("time", System.currentTimeMillis());
                    payload.put("value", object.get("value").toString());

                    Message newMessage = new Message();
                    newMessage.put("topic", topicBuilder.toString());
                    newMessage.put("payload", payload.toString());
                    newMessage.setFlag(true);
                    output(newMessage);
                }
            }
        }

    }
}
