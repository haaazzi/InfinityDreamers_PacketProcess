package com.infinitydreamers;

import com.infinitydreamers.modbus.Injector;
import com.infinitydreamers.modbus.ModbusClient;
import com.infinitydreamers.modbus.ModbusServer;

public class ModbusMain {
    public static void main(String[] args) {
        Injector injector = new Injector("Injector");
        ModbusServer modbusServer = new ModbusServer("ModbusServer");
        ModbusClient modbusClient = new ModbusClient();

        injector.start();
        modbusServer.start();
        modbusClient.start();
    }
}
