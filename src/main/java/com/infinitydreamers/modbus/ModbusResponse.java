package com.infinitydreamers.modbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lombok.extern.slf4j.Slf4j;

/**
 * Modbus Response 생성하는 클래스
 */
@Slf4j
public class ModbusResponse {

    private ModbusResponse() {

    }

    /**
     * 
     * @param functionCode
     * @param quantity
     * @param value
     * @return
     */
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

    /**
     * 
     * @param address
     * @param value
     * @return
     */
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

    /**
     * 
     * @param registers
     * @param address
     * @return
     */
    public static byte[] make16Response(int[] registers, int address) {
        int quantity = registers.length;
        byte[] frame = new byte[5];

        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        b.putInt(address);

        frame[0] = 0x10;

        frame[1] = b.get(2);
        frame[2] = b.get(3);

        frame[3] = 0;
        frame[4] = (byte) quantity;

        return frame;
    }

    /**
     * 
     * @param buffer
     * @return
     */
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

            default:
                log.error("Function code is not 3, 4, 6 or 16!");
                break;
        }

        return addMBAP(transactionId, unitId, result);
    }

    /**
     * 
     * @param transactionId
     * @param unitId
     * @param pdu
     * @return
     */
    public static byte[] addMBAP(int transactionId, int unitId, byte[] pdu) {
        byte[] adu = new byte[7 + pdu.length];
        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);

        b.putInt(transactionId);

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
