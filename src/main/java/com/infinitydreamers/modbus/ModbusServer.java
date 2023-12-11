package com.infinitydreamers.modbus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.github.f4b6a3.uuid.UuidCreator;
import com.infinitydreamers.message.Message;
import com.infinitydreamers.node.InputOutputNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusServer extends InputOutputNode {
    static HashMap<Integer, Integer> map = new HashMap<>();

    /**
     * InnerModbusServer
     */
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

            } finally {
                thread.interrupt();
            }
        }

        public void write(byte[] data) {
            try {
                outputStream.write(data);
                outputStream.flush();
            } catch (IOException ignore) {

            }
        }

        @Override
        public void run() {
            try {
                inputStream = new BufferedInputStream(socket.getInputStream());
                outputStream = new BufferedOutputStream(socket.getOutputStream());

                while (!Thread.currentThread().isInterrupted()) {

                    // Request
                    int length = inputStream.read(buffer);
                    callback.accept(buffer, length);

                    // "[0, 6, 0, 0, 0, 15, 1, 16, 0, 0, 0, 4, 8, 0, 1, 0, 2, 0, 3, 0, 4]"
                    // "data/b/gyeongnam/p/class_a/s/nhnacademy/e/temperature/v/36.5"

                    if (length > 0) {
                        String data = new String(Arrays.copyOfRange(buffer, 0, length));
                        if (data.contains("data")) {
                            int index = data.indexOf("v/");
                            int value = (int) ((Double.parseDouble(data.substring(index + 2, data.length()))) * 10);
                            map.put(100, value);
                        } else {
                            // Response
                            byte[] result = ModbusResponse.getResponse(buffer);
                            System.out.println(Arrays.toString(Arrays.copyOfRange(buffer, 0, 15)));
                            outputStream.write(result);
                            outputStream.flush();
                        }
                    }
                    Thread.sleep(1000);
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                inputStream = null;
                outputStream = null;
            }
        }

    }

    int port = 11502;
    ServerSocket serverSocket;
    Map<UUID, Handler> handlerMap;
    Thread messageReceiver;

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
                                Message message = getInputWire(i).get();
                                // if (message instanceof TcpResponseMessage) {
                                // TcpResponseMessage response = (TcpResponseMessage) message;
                                // Handler handler = getHandler(response.getSenderId());

                                // handler.write(response.getPayload());
                                // }
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
                Message message = new Message();

                if (length > 0) {
                    message.setId(handler.getId());
                    message.put("data", Arrays.toString(Arrays.copyOfRange(data, 0, length)));
                    message.put("length", length.toString());
                    output(message);
                }
            });

            handler.start();

            handlerMap.put(handler.getId(), handler);

        } catch (IOException e) {
            log.error(e.getMessage());
            stop();
        }

    }
}
