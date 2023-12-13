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
import com.infinitydreamers.modbus.Injector;
import com.infinitydreamers.modbus.ModbusClient;
import com.infinitydreamers.modbus.ModbusMapper;
import com.infinitydreamers.modbus.ModbusMaster;
import com.infinitydreamers.mqtt.MQTTClient;
import com.infinitydreamers.mqtt.MqttIn;
import com.infinitydreamers.mqtt.MqttOut;
import com.infinitydreamers.mqtt.MqttPreprocess;
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

            MQTTClient client = new MQTTClient(message);
            MqttIn in = new MqttIn();
            MqttPreprocess mqttPreprocess = new MqttPreprocess();
            TopicNode topicNode = new TopicNode();
            SensorNode sensorNode = new SensorNode();
            MqttOut out = new MqttOut();

            Injector injector = new Injector("Injector");
            ModbusClient modbusClient = new ModbusClient();
            ModbusMaster modbusMaster = new ModbusMaster();
            ModbusMapper modbusMapper = new ModbusMapper();
            RuleEngine ruleEngine = new RuleEngine();

            Wire wire1 = new Wire();
            Wire wire2 = new Wire();
            Wire wire3 = new Wire();
            Wire wire4 = new Wire();
            Wire wire5 = new Wire();

            Wire modWire1 = new Wire();
            Wire modWire2 = new Wire();
            Wire modWire3 = new Wire();

            client.connectOutputWire(wire1);
            in.connectInputWire(wire1);
            in.connectOutputWire(wire2);
            mqttPreprocess.connectInputWire(wire2);
            mqttPreprocess.connectOutputWire(wire3);
            ruleEngine.connectInputWire(wire3);

            modbusClient.connectOutputWire(modWire1);
            modbusMaster.connectInputWire(modWire1);
            modbusMaster.connectOutputWire(modWire2);
            modbusMapper.connectInputWire(modWire2);
            modbusMapper.connectOutputWire(modWire3);
            ruleEngine.connectInputWire(modWire3);

            // topicNode.connectInputWire(wire3);
            // topicNode.connectOutputWire(wire4);

            // sensorNode.connectInputWire(wire4);
            // sensorNode.connectOutputWire(wire5);

            // out.connectInputWire(wire5);
            // out.connectOutputWire(debugWie6);

            client.start();
            in.start();
            mqttPreprocess.start();
            // topicNode.start();
            // sensorNode.start();
            // out.start();

            injector.start();
            modbusClient.start();
            modbusMaster.start();
            modbusMapper.start();
            ruleEngine.start();

        } catch (ParseException | IOException | org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
    }
}
