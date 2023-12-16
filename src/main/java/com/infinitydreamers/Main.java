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
import com.infinitydreamers.modbus.ModbusRtoMMapper;
import com.infinitydreamers.modbus.ModbusServer;
import com.infinitydreamers.modbus.ModbusSlave;
import com.infinitydreamers.modbus.ModbusMaster;
import com.infinitydreamers.modbus.ModbusMtoRMapper;
import com.infinitydreamers.mqtt.MQTTClient;
import com.infinitydreamers.mqtt.MQTTMessageGenerator;
import com.infinitydreamers.mqtt.MqttIn;
import com.infinitydreamers.mqtt.MqttOut;
import com.infinitydreamers.mqtt.MqttPreprocess;
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
            MQTTMessageGenerator mqttMessageGenerator = new MQTTMessageGenerator();
            MqttOut out = new MqttOut();

            Injector injector = new Injector("Injector");
            ModbusClient modbusClient = new ModbusClient();
            ModbusMaster modbusMaster = new ModbusMaster();
            ModbusRtoMMapper modbusRtoMMapper = new ModbusRtoMMapper();
            RuleEngine ruleEngine = new RuleEngine();
            ModbusMtoRMapper modbusMtoRMapper = new ModbusMtoRMapper();
            ModbusSlave modbusSlave = new ModbusSlave();
            ModbusServer modbusServer = new ModbusServer();

            Wire wire1 = new Wire();
            Wire wire2 = new Wire();
            Wire wire3 = new Wire();
            Wire wire4 = new Wire();
            Wire wire5 = new Wire();

            Wire modWire1 = new Wire();
            Wire modWire2 = new Wire();
            Wire modWire3 = new Wire();
            Wire modWire4 = new Wire();
            Wire modWire5 = new Wire();
            Wire modWire6 = new Wire();

            client.connectOutputWire(wire1);
            in.connectInputWire(wire1);
            in.connectOutputWire(wire2);
            mqttPreprocess.connectInputWire(wire2);
            mqttPreprocess.connectOutputWire(wire3);
            ruleEngine.connectInputWire(wire3);
            ruleEngine.connectOutputWire(wire4);
            mqttMessageGenerator.connectInputWire(wire4);
            mqttMessageGenerator.connectOutputWire(wire5);
            out.connectInputWire(wire5);

            modbusClient.connectOutputWire(modWire1);
            modbusMaster.connectInputWire(modWire1);
            modbusMaster.connectOutputWire(modWire2);
            modbusRtoMMapper.connectInputWire(modWire2);
            modbusRtoMMapper.connectOutputWire(modWire3);
            ruleEngine.connectInputWire(modWire3);
            ruleEngine.connectOutputWire(modWire4);
            modbusMtoRMapper.connectInputWire(modWire4);
            modbusMtoRMapper.connectOutputWire(modWire5);
            modbusSlave.connectInputWire(modWire5);
            modbusSlave.connectOutputWire(modWire6);
            modbusServer.connectInputWire(modWire6);

            client.start();
            in.start();
            mqttPreprocess.start();
            mqttMessageGenerator.start();
            out.start();

            injector.start();
            modbusClient.start();
            modbusMaster.start();
            modbusRtoMMapper.start();
            ruleEngine.start();
            modbusMtoRMapper.start();
            modbusSlave.start();
            modbusServer.start();

        } catch (ParseException | IOException | org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
    }
}
