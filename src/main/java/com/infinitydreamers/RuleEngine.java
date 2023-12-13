package com.infinitydreamers;

import java.util.HashMap;
import java.util.Map;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

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
        try (Jedis jedis = pool.getResource()) {
            Map<String, String> hash = new HashMap<>();
            hash.put(message.getJson().get("key").toString(), message.getJson().get("value").toString());
            jedis.hset("value", hash);

            System.out.println(jedis.hgetAll("value"));
            // for (String string : jedis.keys("*")) {
            // System.out.println(string);
            // }
            // System.out.println(jedis.hgetAll("address"));
        }

        output(message);
    }
}
