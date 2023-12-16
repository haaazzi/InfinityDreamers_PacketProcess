package com.infinitydreamers.message;

import java.util.UUID;

import org.json.JSONObject;

/**
 * JSON 페이로드를 갖는 Message
 * JSON 객체에서 데이터를 저장하고 반환
 */
public class Message {
    JSONObject json;

    public Message() {
        this(new JSONObject());
    }

    public Message(JSONObject json) {
        this.json = new JSONObject(json.toString());
    }

    public void put(String key, String value) {
        json.put(key, value);
    }

    public void setId(UUID id) {
        put("Id", id.toString());
    }

    public void setSensor(String sensor) {
        put("sensor", sensor);
    }

    public void setFlag(boolean flag) {
        put("flag", String.valueOf(flag));
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public boolean isFlag() {
        return Boolean.parseBoolean(json.getString("flag"));
    }

    public boolean hasJson() {
        return json != null;
    }
}
