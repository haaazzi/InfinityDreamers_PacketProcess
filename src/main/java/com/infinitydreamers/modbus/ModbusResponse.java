package com.infinitydreamers.modbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusResponse {

    ModbusResponse() {

    }

    public static byte[] make346Response(int functionCode, int quantity, int value) {
        byte[] frame = new byte[2 + quantity * 2];

        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        b.putInt(value);

        frame[0] = (byte) functionCode;

        frame[1] = (byte) (quantity * 2);

        frame[2] = b.get(2);
        frame[3] = b.get(3);

        return frame;
    }

    public static byte[] make6Response(int address, int value) {
        byte[] frame = new byte[5];

        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);

        frame[0] = 0x06;

        b.putInt(address);
        frame[1] = b.get(2);
        frame[2] = b.get(3);

        b.clear();
        b.putInt(value);
        frame[3] = b.get(2);
        frame[4] = b.get(3);

        return frame;
    }

    public static byte[] make16Response(int[] registers, int address) {
        int quantity = registers.length;
        byte[] frame = new byte[5];

        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        b.putInt(address);

        frame[0] = 0x10;

        // Address
        frame[1] = b.get(2);
        frame[2] = b.get(3);

        // Quantity of Registers
        frame[3] = 0;
        frame[4] = (byte) quantity;

        return frame;
    }

    // Response - Read

    public static byte[] getResponse(byte[] buffer) {

        int transactionId = ((buffer[0] << 8) | Byte.toUnsignedInt(buffer[1]));
        int functionCode = buffer[7];
        int unitId = buffer[6];
        int address = ((buffer[8] << 8) | Byte.toUnsignedInt(buffer[9]));
        int quantity = ((buffer[10] << 8) | Byte.toUnsignedInt(buffer[11]));
        int value = ModbusServer.map.containsKey(address) ? ModbusServer.map.get(address) : 0;
        byte[] result = new byte[quantity];

        switch (functionCode) {
            case 3:
            case 4:
            case 6:
                result = make346Response(functionCode, quantity, value);
                break;

            // case 16:
            // result = make16Response(
            // Arrays.copyOfRange(holdingRegisters, address, address + quantity), address);
            // break;

            default:
                log.error("Function code is not 3, 4, 6 or 16!");
                break;
        }

        return addMBAP(transactionId, unitId, result);
    }

    // Header Wrapper - Read + Write
    public static byte[] addMBAP(int transactionId, int unitId, byte[] pdu) {
        byte[] adu = new byte[7 + pdu.length];
        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);

        b.putInt(transactionId);
        // b.get(3));

        adu[0] = b.get(2);
        adu[1] = b.get(3);
        adu[2] = 0;
        adu[3] = 0;
        adu[4] = 0;
        adu[5] = (byte) (pdu.length + 1);
        adu[6] = (byte) unitId;
        System.arraycopy(pdu, 0, adu, 7, pdu.length);

        return adu;
    }

}
