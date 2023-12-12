package com.infinitydreamers;

import com.infinitydreamers.modbus.Injector;
import com.infinitydreamers.modbus.ModbusClient;
import com.infinitydreamers.modbus.ModbusMapper;
import com.infinitydreamers.modbus.ModbusMaster;
import com.infinitydreamers.modbus.ModbusServer;
import com.infinitydreamers.wire.Wire;

public class ModbusMain {
    public static void main(String[] args) {
        Injector injector = new Injector("Injector");
        ModbusClient modbusClient = new ModbusClient();
        ModbusMaster modbusMaster = new ModbusMaster();
        ModbusMapper modbusMapper = new ModbusMapper();

        Wire wire1 = new Wire();
        Wire wire2 = new Wire();

        modbusClient.connectOutputWire(wire1);
        modbusMaster.connectInputWire(wire1);
        modbusMaster.connectOutputWire(wire2);
        modbusMapper.connectInputWire(wire2);

        injector.start();
        modbusClient.start();
        modbusMaster.start();
        modbusMapper.start();
    }
}
