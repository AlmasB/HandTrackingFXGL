package com.almasb.fxglgames.socket;

import com.almasb.fxgl.core.EngineService;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.logging.Logger;
import com.almasb.fxgl.net.ServerConfig;
import com.almasb.fxgl.net.tcp.TCPServer;
import com.almasb.fxglgames.tracking.HandGestureService;
import javafx.util.Duration;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.function.Consumer;

import static com.almasb.fxgl.dsl.FXGL.*;

public class PythonSocketService extends EngineService {

    private static final Logger log = Logger.get(PythonSocketService.class);
    private Consumer<String> messageHandler;

    private PythonSocketServer server;


    @Override
    public void onInit() {
        log.info("Python Socket Service started");


        server = new PythonSocketServer(
                new InetSocketAddress("localhost", 8750),
                this::onMessage);

        server.start();
    }

    @Override
    public void onExit(){
        try {
            server.stop();
        } catch (InterruptedException e) {
            log.warning("Failed to stop server.", e);
        }

    }

    private void onMessage(String message) {
        log.info("Message received: " + message);
        if(Objects.equals(message, "Hello")) {
            server.broadcast("Hello from Gesture Server");
        } else {
            server.broadcast(message);
            String currentGesture = getService(HandGestureService.class).currentGestureProperty().toString();
            server.broadcast(currentGesture);
        }
    }

    private void onClientReady(String s) {
    }


}
