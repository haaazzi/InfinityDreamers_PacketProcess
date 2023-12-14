package com.infinitydreamers;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

import lombok.val;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RuleEngine extends InputOutputNode {
    JedisPool pool = new JedisPool("localhost", 6379);

    @Override
    public void process() {
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            Message message = getInputWire(0).get();
            if (message.isFlag()) {
                updateValue(message);
            }
        } else if ((getInputWire(1) != null) && getInputWire(1).hasMessage()) {
            Message message = getInputWire(1).get();
            if (message.isFlag()) {
                updateValue(message);
            }
        }
    }

    public void updateValue(Message message) {
        String deviceKey = message.getJson().get("key").toString();
        String value = message.getJson().get("value").toString();
        try (Jedis jedis = pool.getResource()) {
            Map<String, String> hash = new HashMap<>();
            hash.put(deviceKey, value);
            jedis.hset("value", hash);
            Map<String, String> resultMap = jedis.hgetAll("place");
            if (resultMap.containsKey(deviceKey)) {
                JSONObject placeObject = new JSONObject(resultMap.get(deviceKey));
                if (placeObject.has("address")) {
                    String address = placeObject.getString("address");
                    JSONObject addressObject = new JSONObject(resultMap.get(address));

                    message.put("place", addressObject.get("place").toString());
                }
            }
        }
        output(message);
    }
}
