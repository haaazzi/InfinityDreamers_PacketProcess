package com.infinitydreamers.modbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusRequest {
    // Request
    public static byte[] make346Request(int address, int quantity, int functionCode) {
        byte[] frame = new byte[5];

        // int -> byte
        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        b.putInt(address);

        // PDU's read function code
        frame[0] = (byte) functionCode;

        // PDU's data
        frame[1] = b.get(2);
        frame[2] = b.get(3);

        b.clear();
        b.putInt(quantity);

        frame[3] = b.get(2);
        frame[4] = b.get(3);

        return frame;
    }

    // Request - Write Multi
    public static byte[] make16Request(int address, int[] registerValues) {
        // 13 minimum
        int quantity = registerValues.length;
        byte[] frame = new byte[6 + 2 * quantity];

        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        b.putInt(address);

        // Function code - 16
        frame[0] = 0x10;

        // Address
        frame[1] = b.get(2);
        frame[2] = b.get(3);

        // Quantity of Registers
        frame[3] = 0;
        frame[4] = (byte) quantity;

        // Byte Count
        frame[5] = (byte) (2 * quantity);

        b.clear();
        // Values
        for (int i = 0; i < quantity; i++) {
            int value = registerValues[i];
            b.putInt(value);
            frame[6 + 2 * i] = b.get(2);
            frame[7 + 2 * i] = b.get(3);
            b.clear();
        }

        return frame;
    }

    // Response - Read

    public static byte[] getRequest(int transactionId, int functionCode, int unitId, int address, int quantity) {

        byte[] result = new byte[quantity];

        switch (functionCode) {
            case 3:
            case 4:
            case 6:
                result = make346Request(address, quantity, functionCode);
                break;

            // case 16:
            // result = make16Request(address, quantity);
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
