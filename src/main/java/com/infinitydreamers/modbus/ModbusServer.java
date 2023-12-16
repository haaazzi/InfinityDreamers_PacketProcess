package com.infinitydreamers.modbus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.github.f4b6a3.uuid.UuidCreator;
import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Modbus 요청을 처리하고 응답을 전송하는 Modbus 서버
 */
@Slf4j
public class ModbusServer extends InputOutputNode {
    static HashMap<Integer, Integer> map = new HashMap<>();

    static class Handler implements Runnable {
        UUID id;
        Thread thread;
        Socket socket;
        BufferedInputStream inputStream;
        BufferedOutputStream outputStream;
        byte[] buffer;
        BiConsumer<byte[], Integer> callback;
        ModbusServer server;

        public Handler(Socket socket, ModbusServer server) {
            id = UuidCreator.getTimeBased();
            thread = new Thread(this);
            buffer = new byte[100000];
            this.server = server;
            this.socket = socket;
        }

        public UUID getId() {
            return id;
        }

        public void setCallback(BiConsumer<byte[], Integer> callback) {
            this.callback = callback;
        }

        public void start() {
            thread.start();
        }

        public void stop() {
            try {
                inputStream.close();
            } catch (IOException ignore) {
                log.error(ignore.getMessage());
            } finally {
                thread.interrupt();
            }
        }

        public void write(byte[] data) {
            try {
                outputStream.write(data);
                outputStream.flush();
            } catch (IOException ignore) {
                log.error(ignore.getMessage());
            }
        }

        @Override
        public void run() {
            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());) {
                this.inputStream = bufferedInputStream;
                this.outputStream = bufferedOutputStream;
                while (!Thread.currentThread().isInterrupted()) {

                    // Request
                    int length = inputStream.read(buffer);
                    callback.accept(buffer, length);
                    if (length > 0 && server.message != null && server.message.getJson().has("response")) {

                        String response = server.message.getJson().getString("response").replace(" ", "");
                        response = response.substring(1, response.length() - 1);
                        String[] stringResult = response.split(",");
                        byte[] result = new byte[stringResult.length];

                        for (int i = 0; i < stringResult.length; i++) {
                            result[i] = Byte.parseByte(stringResult[i]);
                        }

                        outputStream.write(result);
                        outputStream.flush();
                    }
                    Thread.sleep(1000);
                }

                inputStream.close();
                outputStream.close();
                socket.close();

            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                inputStream = null;
                outputStream = null;
            }
        }

    }

    int port = 23456;
    ServerSocket serverSocket;
    Map<UUID, Handler> handlerMap;
    Thread messageReceiver;
    Message message;

    public ModbusServer() {
        this("ModbusServer");
    }

    public ModbusServer(String name) {
        handlerMap = new HashMap<>();
    }

    Handler getHandler(UUID id) {
        return handlerMap.get(id);
    }

    @Override
    public void preprocess() {
        try {
            serverSocket = new ServerSocket(port);

            messageReceiver = new Thread(() -> {

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        for (int i = 0; i < getInputWireCount(); i++) {
                            if ((getInputWire(i) != null) && getInputWire(i).hasMessage()) {
                                message = getInputWire(i).get();
                            }
                        }
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            messageReceiver.start();
        } catch (IOException e) {
            log.error(e.getMessage());
            stop();
        }
    }

    @Override
    public void process() {
        try {
            Socket socket = serverSocket.accept();
            Handler handler = new Handler(socket, this);
            handler.setCallback((data, length) -> {

            });

            handler.start();

            handlerMap.put(handler.getId(), handler);

        } catch (IOException e) {
            log.error(e.getMessage());
            stop();
        }

    }
}
