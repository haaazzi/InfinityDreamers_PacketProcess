package com.infinitydreamers;

import com.infinitydreamers.modbus.Injector;
import com.infinitydreamers.modbus.ModbusClient;
import com.infinitydreamers.modbus.ModbusMapper;
import com.infinitydreamers.modbus.ModbusMaster;
import com.infinitydreamers.wire.Wire;

public class ModbusMain {
    public static void main(String[] args) {
        Injector injector = new Injector("Injector");
        ModbusClient modbusClient = new ModbusClient();
        ModbusMaster modbusMaster = new ModbusMaster();
        ModbusMapper modbusMapper = new ModbusMapper();
        RuleEngine ruleEngine = new RuleEngine();

        Wire modWire1 = new Wire();
        Wire modWire2 = new Wire();
        Wire modWire3 = new Wire();

        modbusClient.connectOutputWire(modWire1);
        modbusMaster.connectInputWire(modWire1);
        modbusMaster.connectOutputWire(modWire2);
        modbusMapper.connectInputWire(modWire2);
        modbusMapper.connectOutputWire(modWire3);
        ruleEngine.connectInputWire(modWire3);

        injector.start();
        modbusClient.start();
        modbusMaster.start();
        modbusMapper.start();
        ruleEngine.start();
    }
}
