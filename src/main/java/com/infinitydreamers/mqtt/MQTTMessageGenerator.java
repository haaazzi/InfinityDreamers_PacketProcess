package com.infinitydreamers.mqtt;

import org.json.JSONObject;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

/**
 * MQTTMessageGenerator 클래스는 입력 메시지를 기반으로 새로운 MQTT 메시지를 생성하는 Node
 */
public class MQTTMessageGenerator extends InputOutputNode {
    @Override
    public void process() {
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            Message message = getInputWire(0).get();
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
