package com.infinitydreamers.modbus;

import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

public class ModbusMapper extends InputOutputNode {
    InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "tig", "tig01#");

    @Override
    public void process() {
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            Message message = getInputWire(0).get();
            if (message.isFlag()) {
                Pong response = this.influxDB.ping();
                if (response.getVersion().equalsIgnoreCase("unknown")) {
                    return;
                }
                String payload = message.getJson().get("payload").toString();
                String[] data = payload.substring(1, payload.length() - 1).replace(" ", "").split(",");

                double value = ((Integer.parseInt(data[10]) << 8) | Integer.parseInt(data[11]) & 0xFF) / 100.0;

                Point point = Point.measurement("memory")
                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .addField("addres", "server1")
                        .addField("free", 4743656L)
                        .addField("used", 1015096L)
                        .addField("buffer", 1010467L)
                        .build();
                influxDB.setDatabase("test");
                influxDB.write(point);
                message.setFlag(true);
                message.put("address", data[6]);
                message.put("value", value + "");
                System.out.println(message.getJson().toString(4));

                output(message);
            }
        }
    }

    public static void main(String[] args) {
        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "tig",
                "tig01#");
        // Pong response = influxDB.ping();
        // if (response.getVersion().equalsIgnoreCase("unknown")) {
        // return;
        // }
        // influxDB.createDatabase("test");
        influxDB.setDatabase("test");

        QueryResult queryResult = influxDB.query(new Query("Select * from memory", "test"));
        queryResult.getResults().forEach(result -> {
            result.getSeries().forEach(series -> {
                series.getValues().forEach(values -> {
                    System.out.println("Values: " + values);
                });
            });
        });
    }
}
