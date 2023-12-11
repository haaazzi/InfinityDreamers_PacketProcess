package com.infinitydreamers;

import com.infinitydreamers.modbus.Injector;
import com.infinitydreamers.modbus.ModbusClient;
import com.infinitydreamers.modbus.ModbusServer;
import com.infinitydreamers.wire.Wire;

public class ModbusMain {
    public static void main(String[] args) {
        Injector injector = new Injector("Injector");
        ModbusServer modbusServer = new ModbusServer("ModbusServer");
        ModbusClient modbusClient = new ModbusClient();

        Wire wire1 = new Wire();
        Wire wire2 = new Wire();

        injector.connectOutputWire(wire1);
        modbusServer.connectInputWire(wire1);
        modbusServer.connectOutputWire(wire2);
        modbusClient.connectInputWire(wire2);

        injector.start();
        modbusServer.start();
        modbusClient.start();
    }
}
