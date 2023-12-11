package com.infinitydreamers.modbus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import com.infinitydreamers.node.InputOutputNode;

public class ModbusClient extends InputOutputNode {
    int transactionId = 0;

    @Override
    public void process() {
        try (Socket socket = new Socket("172.18.0.1", 11502)) {
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());

            byte[] buffer = new byte[1024];

            byte[] request = ModbusRequest.getRequest(1, 3, 1, 100, 1);

            outputStream.write(request);
            outputStream.flush();

            Thread.sleep(1000);

            int length = inputStream.read(buffer);
            byte[] result = (Arrays.copyOfRange(buffer, 0, length));

            double value = ((result[9] << 8) | result[10] & 0xFF) / 10.0;

            Thread.sleep(5000);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

    }

}
