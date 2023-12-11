package com.infinitydreamers.node;

import java.util.ArrayList;
import java.util.List;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.wire.Wire;

public class InputOutputNode extends ActiveNode {
    List<Wire> inputWires;
    List<Wire> outputWires;
    static int successCount = 0;
    static int failCount = 0;

    public InputOutputNode(String name) {
        super(name);

        inputWires = new ArrayList<>();
        outputWires = new ArrayList<>();
    }

    public InputOutputNode() {
        super();

        inputWires = new ArrayList<>();
        outputWires = new ArrayList<>();
    }

    public void connectInputWire(Wire wire) {
        inputWires.add(wire);
    }

    public void connectOutputWire(Wire wire) {
        outputWires.add(wire);
    }

    public void output(Message message) {
        for (Wire wire : outputWires) {
            wire.put(message);
        }
    }

    public int getInputWireCount() {
        return inputWires.size();
    }

    public int getOutputWireCount() {
        return outputWires.size();
    }

    public Wire getInputWire(int index) {
        return inputWires.get(index);
    }

    public Wire getOutputWire(int index) {
        return outputWires.get(index);
    }

    public static void increaseSuccess() {
        successCount++;
    }

    public static void increasefail() {
        failCount++;
    }
}
