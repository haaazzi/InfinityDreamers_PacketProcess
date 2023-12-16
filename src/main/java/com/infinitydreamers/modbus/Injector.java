package com.infinitydreamers.modbus;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import com.infinitydreamers.node.OutputNode;

/**
 * Modbus 응답을 보내는 Node
 */
public class Injector extends OutputNode {
    Random random = new Random();

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
                    ModbusResponse.make6Response(200, (random.nextInt(4000 + 1000))));

            Thread.sleep(5000);
            outputStream.write(request);
            outputStream.flush();

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}
