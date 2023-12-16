package com.infinitydreamers.modbus;

import java.util.Map;

import org.json.JSONObject;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Message에 저장된 payload를 Modbus Response로 매핑해 key, value를 생성하는 Node
 */
public class ModbusRtoMMapper extends InputOutputNode {
    JedisPool pool = new JedisPool("localhost", 6379);

    @Override
    public void process() {
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            Message message = getInputWire(0).get();
            if (message.isFlag()) {
                String payload = message.getJson().get("payload").toString();
                String[] data = payload.substring(1, payload.length() - 1).replace(" ", "").split(",");

                double value = ((Integer.parseInt(data[10]) << 8) | Integer.parseInt(data[11]) & 0xFF) / 100.0;

                message.setFlag(true);
                message.put("value", value + "");
                try (Jedis jedis = pool.getResource()) {
                    Map<String, String> resultMap = jedis.hgetAll("address");

                    JSONObject object = new JSONObject(resultMap.get((Byte.parseByte(data[9]) & 0xff) + ""));
                    String deviceId = object.get("deviceId").toString();
                    String type = object.get("type").toString();

                    message.put("key", deviceId + "-" + type);
                }
                output(message);
            }
        }
    }

}