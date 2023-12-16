package com.infinitydreamers;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.json.JSONObject;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.wire.Wire;

public class RuleEngineTest {

    private RuleEngine ruleEngine;
    private Message message;
    private JSONObject json;

    @Test
    public void testUpdateValue() {
        ruleEngine = new RuleEngine();

        message = new Message();

        json = new JSONObject();
        json.put("key", "24e124128c067999-temperature");
        json.put("value", "testValue");
        message.setJson(json);
        message.setFlag(true);

        Wire inputWire = new Wire();
        inputWire.put(message);

        ruleEngine.connectInputWire(inputWire);
        ruleEngine.process();

        String expected = "nhnacademy/gyeongnam/class_a";
        String actual = message.getJson().get("place").toString();
        assertEquals(expected, actual);
    }
}