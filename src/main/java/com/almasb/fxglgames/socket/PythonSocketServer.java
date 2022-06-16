package com.almasb.fxglgames.socket;

import com.almasb.fxgl.logging.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.function.Consumer;


public class PythonSocketServer extends WebSocketServer {

    private static final Logger log = Logger.get(PythonSocketServer.class);

    private Consumer<String> messageHandler;
    private Runnable connectHandler;

    public PythonSocketServer(InetSocketAddress address, Consumer<String> messageHandler, Runnable connectHandler) {
        super(address);
        this.messageHandler = messageHandler;
        this.connectHandler = connectHandler;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        log.info("new connection to " + webSocket.getRemoteSocketAddress());
        webSocket.send("Connected to Gesture Server");
        connectHandler.run();
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        //log.info("connection closed to " + webSocket.getRemoteSocketAddress());
        log.info("Client disconnected");
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        messageHandler.accept(s);
    }

    @Override
    public void onError(WebSocket webSocket, Exception ex) {
        try {
            log.warning("an error occurred on connection " + webSocket.getRemoteSocketAddress(), ex);
        } catch (Exception e) {
            log.warning("Socket Error: ", ex);
            log.warning("an error occurred on python server", e);
        }
    }

    @Override
    public void onStart() {
        log.info("Python Socket Server started");
    }
}
