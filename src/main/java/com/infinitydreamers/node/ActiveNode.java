package com.infinitydreamers.node;

import java.util.UUID;

public abstract class ActiveNode extends Node implements Runnable {
    Thread thread;

    ActiveNode() {
        super();
    }

    ActiveNode(String name) {
        super(name);
    }

    ActiveNode(String name, UUID id) {
        super(name, id);
    }

    public synchronized void start() {
        thread = new Thread(this, getName());
        thread.start();
    }

    public synchronized void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void preprocess() {

    }

    public void process() {

    }

    public void postprocess() {

    }

    @Override
    public void run() {
        preprocess();
        while (thread.isAlive()) {
            process();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        postprocess();

    }

}
