package com.infinitydreamers.node;

import java.util.ArrayList;
import java.util.List;

import com.infinitydreamers.message.Message;
import com.infinitydreamers.wire.Wire;

public abstract class OutputNode extends ActiveNode {
    List<Wire> outputWires;

    public OutputNode(String name) {
        super(name);

        outputWires = new ArrayList<>();
    }

    public void connectOutputWire(Wire wire) {
        outputWires.add(wire);
    }

    public int getOutputWireCount() {
        return outputWires.size();
    }

    public Wire getOutputWire(int index) {
        return outputWires.get(index);
    }

    public void output(Message message) {
        for (Wire wire : outputWires) {
            wire.put(message);
        }
    }
}
