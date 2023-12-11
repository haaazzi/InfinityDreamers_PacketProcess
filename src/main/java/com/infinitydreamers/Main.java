package com.infinitydreamers;

import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.mqtt.DeviceInfoNode;
import com.infinitydreamers.mqtt.MqttIn;
import com.infinitydreamers.mqtt.MqttOut;
import com.infinitydreamers.mqtt.SensorNode;
import com.infinitydreamers.mqtt.TopicNode;
import com.infinitydreamers.node.DebugNode;
import com.infinitydreamers.wire.Wire;

public class Main {
    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.addOption("an", "application", true, "application name");
            options.addOption("s", "sensor", true, "sensor");
            options.addOption("c", "configure", true, "configure");
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine;
            commandLine = parser.parse(options, args);

            String topicFilter = "";
            String sensor = "";
            String filePath = "";
            Message message = new Message();

            if (commandLine.hasOption("an")) {
                topicFilter = commandLine.getOptionValue("an");
                message.put("inputTopic", topicFilter);

            } else if (commandLine.hasOption("s")) {
                sensor = commandLine.getOptionValue("s");
                message.setSensor(sensor);

            } else if (commandLine.hasOption("c")) {
                filePath = commandLine.getOptionValue("c");
                JSONParser jsonParser = new JSONParser();
                Object obj = jsonParser.parse(new FileReader("./" + filePath));
                JSONObject jsonObject = (JSONObject) obj;

                if (jsonObject.containsKey("uri")) {
                    message.put("uri", jsonObject.get("uri").toString());
                }

                if (jsonObject.containsKey("inputTopic")) {
                    message.put("inputTopic", (String) jsonObject.get("inputTopic"));
                }

                if (jsonObject.containsKey("sensor")) {
                    message.setSensor((String) jsonObject.get("sensor"));
                }

                if (jsonObject.containsKey("outputTopic")) {
                    message.put("outputTopic", jsonObject.get("outputTopic").toString());
                }

            }

            MqttIn in = new MqttIn(message);
            DeviceInfoNode deviceInfo = new DeviceInfoNode();
            TopicNode topicNode = new TopicNode();
            SensorNode sensorNode = new SensorNode();
            MqttOut out = new MqttOut();
            DebugNode debugNode1 = new DebugNode("MqttIn");
            DebugNode debugNode3 = new DebugNode("DeviceNode");
            DebugNode debugNode4 = new DebugNode("TopicNode");
            DebugNode debugNode5 = new DebugNode("SensorNode");
            DebugNode debugNode6 = new DebugNode("MqttOut");

            Wire wire2 = new Wire();
            Wire wire3 = new Wire();
            Wire wire4 = new Wire();
            Wire wire5 = new Wire();

            Wire debugWire2 = new Wire();
            Wire debugWire3 = new Wire();
            Wire debugWire4 = new Wire();
            Wire debugWire5 = new Wire();
            Wire debugWire6 = new Wire();

            in.connectOutputWire(wire2);
            in.connectOutputWire(debugWire2);
            debugNode1.connectInputWire(debugWire2);

            deviceInfo.connectInputWire(wire2);
            deviceInfo.connectOutputWire(wire3);
            deviceInfo.connectOutputWire(debugWire3);
            debugNode3.connectInputWire(debugWire3);

            topicNode.connectInputWire(wire3);
            topicNode.connectOutputWire(wire4);
            topicNode.connectOutputWire(debugWire4);
            debugNode4.connectInputWire(debugWire4);

            sensorNode.connectInputWire(wire4);
            sensorNode.connectOutputWire(wire5);
            sensorNode.connectOutputWire(debugWire5);
            debugNode5.connectInputWire(debugWire5);

            out.connectInputWire(wire5);
            out.connectOutputWire(debugWire6);
            debugNode6.connectInputWire(debugWire6);

            in.start();
            deviceInfo.start();
            topicNode.start();
            sensorNode.start();
            out.start();
            debugNode1.start();
            debugNode3.start();
            debugNode4.start();
            debugNode5.start();
            debugNode6.start();
        } catch (ParseException | IOException | org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
    }
}
