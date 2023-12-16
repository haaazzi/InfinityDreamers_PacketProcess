package com.infinitydreamers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.json.JSONObject;
import org.junit.Test;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.mqtt.MQTTClient;
import com.infinitydreamers.mqtt.MQTTMessageGenerator;
import com.infinitydreamers.mqtt.MqttIn;
import com.infinitydreamers.mqtt.MqttPreprocess;
import com.infinitydreamers.wire.Wire;

public class MqttTest {
    @Test
    public void testMqttClient() throws InterruptedException, IllegalArgumentException, IllegalAccessException {
        Wire wire = new Wire();
        Message message = new Message();
        message.put("inputTopic", "testTopic");
        message.put("sensor", "testSensor");
        MQTTClient mqttClient = new MQTTClient(message);

        mqttClient.connectOutputWire(wire);

        mqttClient.preprocess();

        Field field;
        JSONObject object;
        try {
            field = mqttClient.getClass().getDeclaredField("message");
            field.setAccessible(true);
            object = ((Message) field.get(mqttClient)).getJson();

            assertEquals("testTopic", object.getString("inputTopic"));
            assertEquals("testSensor", object.getString("sensor"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMqttIn() throws InterruptedException, IllegalArgumentException, IllegalAccessException {
        MqttIn mqttIn = new MqttIn();
        Wire wire1 = new Wire();
        Wire wire2 = new Wire();

        Message message = new Message();
        message.setFlag(true);
        message.put("topic", "device/topic");

        wire1.put(message);

        mqttIn.connectInputWire(wire1);
        mqttIn.connectOutputWire(wire2);
        // Act
        mqttIn.process();

        // Assert
        Message outputMessage = wire2.get();
        assertEquals("device/topic",
                outputMessage.getJson().get("topic").toString());
    }

    @Test
    public void testMqttMessageGenerator() {
        MQTTMessageGenerator messageGenerator = new MQTTMessageGenerator();
        JSONObject inputJson = new JSONObject();
        inputJson.put("place", "test/example/place/key");
        inputJson.put("key", "device-key");
        inputJson.put("value", 42);

        Wire wire1 = new Wire();
        Wire wire2 = new Wire();

        Message inputMessage = new Message(inputJson);
        inputMessage.setFlag(true);

        wire1.put(inputMessage);
        messageGenerator.connectInputWire(wire1);
        messageGenerator.connectOutputWire(wire2);

        messageGenerator.process();

        assertTrue(wire2.hasMessage());
        Message outputMessage = wire2.get();
        assertTrue(outputMessage.isFlag());
        assertTrue(outputMessage.hasJson());
        assertTrue(outputMessage.getJson().has("topic"));
        assertTrue(outputMessage.getJson().has("payload"));

        assertEquals("s/test/b/example/p/place/e/key", outputMessage.getJson().getString("topic"));

        JSONObject payloadJson = new JSONObject(outputMessage.getJson().getString("payload"));
        assertTrue(payloadJson.has("time"));
        assertTrue(payloadJson.has("value"));
        assertEquals(42, payloadJson.getInt("value"));
    }

    @Test
    public void testMqttPreprocess() {
        MqttPreprocess mqttPreprocess = new MqttPreprocess();
        Wire inputWire = new Wire();
        Wire outputWire = new Wire();

        mqttPreprocess.connectInputWire(inputWire);
        mqttPreprocess.connectOutputWire(outputWire);

        JSONObject deviceInfoJson = new JSONObject();
        deviceInfoJson.put("devEui", "24e124136d151368");
        deviceInfoJson.put("tags", new JSONObject(
                "{\"place\":\"server_room\",\"name\":\"서버실\",\"branch\":\"gyeongnam\",\"site\":\"nhnacademy\"}"));

        JSONObject inputJson = new JSONObject();
        inputJson.put("deviceInfo", deviceInfoJson);
        inputJson.put("object", new JSONObject(
                "{\"temperature\": 24.200000000000003}"));

        Message inputMessage = new Message();
        inputMessage.setFlag(true);
        inputMessage.put("payload", inputJson.toString());

        inputWire.put(inputMessage);

        mqttPreprocess.process();

        Message outputMessage = outputWire.get();

        assertTrue(outputMessage.isFlag());
        assertTrue(outputMessage.hasJson());
        assertTrue(outputMessage.getJson().has("key"));
        assertTrue(outputMessage.getJson().has("value"));

        assertEquals("24e124136d151368-temperature", outputMessage.getJson().getString("key"));
    }
}
