package com.infinitydreamers.modbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lombok.extern.slf4j.Slf4j;

/**
 * Modbus Request을 생성하는 클래스
 */
@Slf4j
public class ModbusRequest {
    private ModbusRequest() {

    }

    /**
     * 
     * @param address
     * @param quantity
     * @param functionCode
     * @return
     */
    public static byte[] make346Request(int address, int quantity, int functionCode) {
        byte[] frame = new byte[5];

        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        b.putInt(address);

        frame[0] = (byte) functionCode;

        frame[1] = b.get(2);
        frame[2] = b.get(3);

        b.clear();
        b.putInt(quantity);

        frame[3] = b.get(2);
        frame[4] = b.get(3);

        return frame;
    }

    /**
     * 
     * @param address
     * @param registerValues
     * @return
     */
    public static byte[] make16Request(int address, int[] registerValues) {
        int quantity = registerValues.length;
        byte[] frame = new byte[6 + 2 * quantity];

        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        b.putInt(address);

        frame[0] = 0x10;

        frame[1] = b.get(2);
        frame[2] = b.get(3);

        frame[3] = 0;
        frame[4] = (byte) quantity;

        frame[5] = (byte) (2 * quantity);

        b.clear();

        for (int i = 0; i < quantity; i++) {
            int value = registerValues[i];
            b.putInt(value);
            frame[6 + 2 * i] = b.get(2);
            frame[7 + 2 * i] = b.get(3);
            b.clear();
        }

        return frame;
    }

    /**
     * 
     * @param transactionId
     * @param functionCode
     * @param unitId
     * @param address
     * @param quantity
     * @return
     */
    public static byte[] getRequest(int transactionId, int functionCode, int unitId, int address, int quantity) {

        byte[] result = new byte[quantity];

        switch (functionCode) {
            case 3:
            case 4:
            case 6:
                result = make346Request(address, quantity, functionCode);
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
