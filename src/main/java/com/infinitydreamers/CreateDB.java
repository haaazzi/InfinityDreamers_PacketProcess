package com.infinitydreamers;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * CreateDB 클래스는 데이터베이스를 생성하고 초기화하는 역할을 담당합니다.
 */
public class CreateDB {
    public static void main(String[] args) {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            Map<String, Map<String, String>> pp = new HashMap<>();

            Map<String, String> hash = new HashMap<>();
            hash.put("deviceId", "24e124128c067999");
            hash.put("type", "temperature");
            hash.put("place", "nhnacademy/gyeongnam/class_a");
            pp.put("100", hash);

            hash = new HashMap<>();
            hash.put("deviceId", "24e124785c389818");
            hash.put("type", "temperature");
            hash.put("place", "nhnacademy/gyeongnam/class_a");
            pp.put("101", hash);

            hash = new HashMap<>();
            hash.put("deviceId", "24e124785c421885");
            hash.put("type", "temperature");
            hash.put("place", "nhnacademy/gyeongnam/class_a");
            pp.put("102", hash);

            hash = new HashMap<>();
            hash.put("deviceId", "24e124785c389818");
            hash.put("type", "humidity");
            hash.put("place", "nhnacademy/gyeongnam/class_a");
            pp.put("200", hash);

            hash = new HashMap<>();
            hash.put("deviceId", "24e124785c421885");
            hash.put("type", "humidity");
            hash.put("place", "nhnacademy/gyeongnam/class_a");
            pp.put("201", hash);

            for (Map.Entry<String, Map<String, String>> entry : pp.entrySet()) {
                String key = entry.getKey();
                Map<String, String> hash1 = entry.getValue();

                jedis.hset("address", key, new Gson().toJson(hash1));
            }

            Map<String, String> hash2 = new HashMap<>();
            hash2.put("address", "100");
            hash2.put("unitId", "1");
            pp.put("24e124128c067999-temperature", hash2);

            hash2 = new HashMap<>();
            hash2.put("address", "101");
            hash2.put("unitId", "1");
            pp.put("24e124785c389818-temperature", hash2);

            hash2 = new HashMap<>();
            hash2.put("address", "102");
            hash2.put("unitId", "1");
            pp.put("24e124785c421885-temperature", hash2);

            hash2 = new HashMap<>();
            hash2.put("address", "200");
            hash2.put("unitId", "1");
            pp.put("24e124785c389818-humidity", hash2);

            hash2 = new HashMap<>();
            hash2.put("address", "201");
            hash2.put("unitId", "1");
            pp.put("24e124785c421885-humidity", hash2);

            for (Map.Entry<String, Map<String, String>> entry : pp.entrySet()) {
                String key = entry.getKey();
                Map<String, String> hash1 = entry.getValue();

                jedis.hset("place", key, new Gson().toJson(hash1));
            }

            jedis.save();
            pool.close();
        }
    }
}