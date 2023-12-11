package com.infinitydreamers.node;

import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Node {
    static int count = 0;
    UUID id;
    String name;
    String className = getClass().getSimpleName();

    Node() {
        this(UuidCreator.getTimeBased());
    }

    Node(UUID id) {
        this(id.toString(), id);
    }

    Node(String name) {
        this(name, UuidCreator.getTimeBased());
    }

    Node(String name, UUID id) {
        this.id = id;
        this.name = name;

        log.info("create node : " + getId());
    }

    public static int getCount() {
        return count;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}