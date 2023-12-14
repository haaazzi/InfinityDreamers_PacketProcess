package com.infinitydreamers.modbus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

public class ModbusClient extends InputOutputNode {
    int transactionId = 0;
    Socket socket;

    @Override
    public void process() {
        try {
            socket = new Socket("localhost", 1234);
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
            Message message = new Message();

            byte[] buffer = new byte[1024];

            int length = inputStream.read(buffer);
            byte[] result = (Arrays.copyOfRange(buffer, 0, length));

            message.put("payload", Arrays.toString(result));
            message.setFlag(true);
            Thread.sleep(1000);
            output(message);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

    }

}
