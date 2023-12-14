package com.infinitydreamers.modbus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ModbusMtoRMapper extends InputOutputNode {
    JedisPool pool = new JedisPool("localhost", 6379);
    int transactionId = 0;

    @Override
    public void process() {
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            Message message = getInputWire(0).get();

            if (message.isFlag()) {
                String key = message.getJson().getString("key");

                try (Jedis jedis = pool.getResource()) {
                    System.out.println(message.getJson().toString(4));
                    Map<String, String> resultMap = jedis.hgetAll("place");
                    String request = "";
                    if (resultMap.containsKey(key)) {
                        JSONObject object = new JSONObject(resultMap.get(key));
                        int address = Integer.parseInt(object.getString("address"));
                        int unitId = Integer.parseInt(object.getString("unitId"));
                        request = Arrays
                                .toString(ModbusRequest.getRequest(++transactionId, 3, unitId, address, 1));
                    }
                    message.put("request", request);
                }
                output(message);
            }
        }
    }

}