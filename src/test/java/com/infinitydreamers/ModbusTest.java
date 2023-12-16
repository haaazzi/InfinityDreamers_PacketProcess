package com.infinitydreamers;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import org.json.JSONObject;
import org.junit.Test;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.modbus.Injector;
import com.infinitydreamers.modbus.ModbusClient;
import com.infinitydreamers.modbus.ModbusMaster;
import com.infinitydreamers.modbus.ModbusMtoRMapper;
import com.infinitydreamers.modbus.ModbusRequest;
import com.infinitydreamers.modbus.ModbusResponse;
import com.infinitydreamers.modbus.ModbusRtoMMapper;
import com.infinitydreamers.modbus.ModbusServer;
import com.infinitydreamers.modbus.ModbusSlave;
import com.infinitydreamers.wire.Wire;

public class ModbusTest {
    @Test
    public void testInjector() {
        Thread serverThread = new Thread(() -> {
            Injector injector = new Injector("TestInjector");
            injector.start();

            try (Socket socket = new Socket("localhost", 123)) {
                Thread.sleep(1000);

                assertTrue(socket.isConnected());

                InputStream inputStream = socket.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                byte[] responseBuffer = new byte[1024];
                int bytesRead = bufferedInputStream.read(responseBuffer);

                assertFalse(bytesRead == -1);

                socket.close();
                injector.stop();
            } catch (IOException | InterruptedException e) {
                fail("Exception thrown: " + e.getMessage());
            }
        });

        serverThread.start();

        try {
            serverThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted: " + e.getMessage());
        }
    }

    @Test
    public void testModbusClientResponse() {
        ModbusClient modbusClient = new ModbusClient();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 });
        try (Socket testSocket = new Socket("localhost", 4321) {
            @Override
            public OutputStream getOutputStream() {
                return outputStream;
            }

            @Override
            public InputStream getInputStream() {
                return inputStream;
            }
        }) {
            try {
                byte[] expectedBytes = { 0, 1, 0, 0, 0, 6, 1, 3, 0, 0, 0, 5 };

                modbusClient.process();

                ByteArrayOutputStream writtenBytesStream = (ByteArrayOutputStream) testSocket.getOutputStream();
                byte[] writtenBytes = writtenBytesStream.toByteArray();
                assertArrayEquals(expectedBytes, writtenBytes);
                testSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testModbusMasterProcessing() {
        Message inputMessage = new Message();
        inputMessage.setFlag(true);

        ModbusMaster modbusMaster = new ModbusMaster();
        Wire inputWire = new Wire();
        inputWire.put(inputMessage);
        modbusMaster.connectInputWire(inputWire);

        modbusMaster.process();

        assertTrue(modbusMaster.getInputWireCount() > 0);
        Message outputMessage = modbusMaster.getInputWire(0).get();
        if (outputMessage != null) {
            assertTrue(outputMessage.isFlag());
        }
    }

    @Test
    public void testModbusRtoMMapper() {
        ModbusRtoMMapper modbusRtoMMapper = new ModbusRtoMMapper();
        Wire inputWire = new Wire();
        Wire outputWire = new Wire();

        modbusRtoMMapper.connectInputWire(inputWire);
        modbusRtoMMapper.connectOutputWire(outputWire);

        Message message = new Message();
        message.setFlag(true);
        JSONObject json = new JSONObject();
        json.put("payload", "[0, 1, 0, 0, 0, 6, 1, 6, 0, 100, 13, 73]"); // Adjust as needed
        message.setJson(json);
        message.setFlag(true);

        inputWire.put(message);

        modbusRtoMMapper.process();

        Message outputMessage = outputWire.get();

        assertEquals("34.01", outputMessage.getJson().getString("value"));
        assertEquals("24e124128c067999-temperature", outputMessage.getJson().getString("key"));
    }

    @Test
    public void testModbusServer() {
        ModbusServer modbusServer;

        modbusServer = new ModbusServer();
        assertNotNull(modbusServer);

        Thread serverThread = new Thread(() -> {
            modbusServer.process();
        });

        serverThread.start();
        try {
            Thread.sleep(2000); // Adjust the sleep time based on your server processing time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        modbusServer.stop();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testModbusSlave() {
        ModbusSlave modbusSlave = new ModbusSlave();
        Wire inputWire = new Wire();
        Wire outputWire = new Wire();

        modbusSlave.connectInputWire(inputWire);
        modbusSlave.connectOutputWire(outputWire);

        Message message = new Message();
        message.setFlag(true);

        inputWire.put(message);

        modbusSlave.process();

        Message outputMessage = outputWire.get();

        assertTrue(outputMessage.isFlag());
    }

    @Test
    public void ModbusMtoRMapperTest() {
        ModbusMtoRMapper mtormapper = new ModbusMtoRMapper();
        Wire inputwire = new Wire();
        Wire outputwire = new Wire();

        Message message = new Message();
        message.setFlag(true);
        message.put("value", "1999");
        message.put("key", "24e124126d152590-pressure");
        inputwire.put(message);

        mtormapper.connectInputWire(inputwire);
        mtormapper.connectOutputWire(outputwire);
        mtormapper.start();
        if (mtormapper.getOutputWire(0) != null && mtormapper.getOutputWire(0).hasMessage()) {
            Message result = mtormapper.getOutputWire(0).get();

            assertAll(
                    () -> assertTrue(result.getJson().has("response")),
                    () -> assertTrue(result.getJson().has("place")),
                    () -> assertTrue(result.isFlag()));
        }
        mtormapper.stop();
    }

    @Test
    public void ModbusRequestTest() {
        byte[] request3 = ModbusRequest.getRequest(1, 3, 1, 1, 1);
        byte[] request4 = ModbusRequest.getRequest(1, 4, 1, 1, 1);
        byte[] request6 = ModbusRequest.getRequest(1, 6, 1, 1, 1);

        byte[] expected3 = { 0, 1, 0, 0, 0, 6, 1, 3, 0, 1, 0, 1 };
        byte[] expected4 = { 0, 1, 0, 0, 0, 6, 1, 4, 0, 1, 0, 1 };
        byte[] expected6 = { 0, 1, 0, 0, 0, 6, 1, 6, 0, 1, 0, 1 };

        assertAll(
                () -> assertTrue(Arrays.equals(expected3, request3)),
                () -> assertTrue(Arrays.equals(expected4, request4)),
                () -> assertTrue(Arrays.equals(expected6, request6)));
    }

    @Test
    public void ModbusResponseTest() {
        byte[] response3 = ModbusResponse.getResponse(new byte[] { 0, 1, 0, 0, 0, 5, 1, 3, 0, 1, 0, 1 });
        byte[] response4 = ModbusResponse.getResponse(new byte[] { 0, 1, 0, 0, 0, 5, 1, 4, 0, 1, 0, 1 });
        byte[] response6 = ModbusResponse.addMBAP(1, 1, ModbusResponse.make6Response(1, 1));

        byte[] expected3 = { 0, 1, 0, 0, 0, 5, 1, 3, 2, 0, 0 };
        byte[] expected4 = { 0, 1, 0, 0, 0, 5, 1, 4, 2, 0, 0 };
        byte[] expected6 = { 0, 1, 0, 0, 0, 6, 1, 6, 0, 1, 0, 1 };

        assertAll(
                () -> assertTrue(Arrays.equals(expected3, response3)),
                () -> assertTrue(Arrays.equals(expected4, response4)),
                () -> assertTrue(Arrays.equals(expected6, response6)));
    }
}
