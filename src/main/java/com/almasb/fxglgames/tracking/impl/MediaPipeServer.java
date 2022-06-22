/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxglgames.tracking.impl;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

import com.almasb.fxgl.logging.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

final class MediaPipeServer extends WebSocketServer {

    private static final Logger log = Logger.get(MediaPipeServer.class);
    private Consumer<String> messageHandler;

    public MediaPipeServer(InetSocketAddress address, Consumer<String> messageHandler) {
        super(address);
        this.messageHandler = messageHandler;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        log.info("new connection to " + conn.getRemoteSocketAddress());

        // TODO:
//		conn.send("Welcome to the server!"); //This method sends a message to the new client
//		broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This method sends a message to all clients connected
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        log.info("connection from mediapipe client closed with code=" + code);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        messageHandler.accept(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        try {
            log.warning("an error occurred on connection " + conn.getRemoteSocketAddress(), ex);
        } catch (Exception e) {
            log.warning("Socket Error: ", ex);
            log.warning("an error occurred on mediapipe server", e);
        }
    }

    @Override
    public void onStart() {
        log.info("Server started successfully");
    }
}