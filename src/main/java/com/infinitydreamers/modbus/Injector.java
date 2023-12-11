package com.infinitydreamers.modbus;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.infinitydreamers.node.OutputNode;

public class Injector extends OutputNode {

    public Injector(String name) {
        super(name);
    }

    int transactionId = 0;

    @Override
    public void process() {
        try (Socket socket = new Socket("172.18.0.1", 11502)) {
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());

            byte[] request = "data/b/gyeongnam/p/class_a/s/nhnacademy/e/temperature/v/36.5".getBytes();

            outputStream.write(request);
            outputStream.flush();

            Thread.sleep(5000);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

    }
}
