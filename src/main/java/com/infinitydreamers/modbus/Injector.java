package com.infinitydreamers.modbus;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.infinitydreamers.node.OutputNode;

public class Injector extends OutputNode {

    public Injector(String name) {
        super(name);
    }

    int transactionId = 0;

    @Override
    public void process() {
        try (ServerSocket serverSocket = new ServerSocket(1234);
                Socket socket = serverSocket.accept()) {
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            byte[] request = ModbusResponse.addMBAP(++transactionId, 1,
                    ModbusResponse.make6Response(200, ((int) (Math.random() * 3000) + 1000)));

            Thread.sleep(5000);
            outputStream.write(request);
            outputStream.flush();

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}
