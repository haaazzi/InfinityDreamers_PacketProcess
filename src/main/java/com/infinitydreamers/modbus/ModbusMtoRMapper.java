package com.infinitydreamers.modbus;

import java.util.Arrays;
import java.util.Map;

import org.json.JSONObject;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Modbus 메시지를 Redis로 매핑하고 Message에 Response 담는 Node
 */
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
                    Map<String, String> resultMap = jedis.hgetAll("place");
                    byte[] response = null;
                    if (resultMap.containsKey(key)) {
                        JSONObject object = new JSONObject(resultMap.get(key));
                        int unitId = Integer.parseInt(object.getString("unitId"));

                        resultMap = jedis.hgetAll("value");
                        int value = (int) (Double.parseDouble(resultMap.get(key)) * 100);

                        response = ModbusResponse.addMBAP(++transactionId, unitId,
                                ModbusResponse.make346Response(3, 1, value));
                        message.put("response", Arrays.toString(response));
                        output(message);
                    }
                }
            }
        }
    }

}