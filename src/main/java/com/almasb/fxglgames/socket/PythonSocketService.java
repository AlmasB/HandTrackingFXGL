package com.almasb.fxglgames.socket;

import com.almasb.fxgl.core.EngineService;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.logging.Logger;
import com.almasb.fxgl.net.ServerConfig;
import com.almasb.fxgl.net.tcp.TCPServer;
import com.almasb.fxglgames.tracking.HandGestureService;
import javafx.util.Duration;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.function.Consumer;

import static com.almasb.fxgl.dsl.FXGL.*;

public class PythonSocketService extends EngineService {

    private static final Logger log = Logger.get(PythonSocketService.class);
    private Consumer<String> messageHandler;
    private Consumer<WebSocket> connectHandler;

    private PythonSocketServer server;


    @Override
    public void onInit() {
        log.info("Python Socket Service started");


        server = new PythonSocketServer(
                new InetSocketAddress("localhost", 8750),
                this::onMessage,
                this::onConnect);

        server.start();
    }

    public void onConnect() {
        // Broadcast on delay
        getGameTimer().runAtInterval(() -> {
            try {
                String currentGesture = String.valueOf(getService(HandGestureService.class).currentGestureProperty().get());
                server.broadcast(currentGesture);
                log.info("Gesture Sent");
            } catch (Exception e) {
                log.warning("Failed to broadcast gesture.", e);
            }
        }, Duration.seconds(3));
    }

    @Override
    public void onExit(){
        try {
            server.stop();
        } catch (InterruptedException e) {
            log.warning("Failed to stop server.", e);
        }
    }

    @Override
    public void onUpdate(double tpf) {
        // Broadcast on frame
//            try {
//                String currentGesture = getService(HandGestureService.class).currentGestureProperty().toString();
//                server.broadcast(currentGesture);
//                log.info("Gesture Sent");
//            } catch (Exception e) {
//                log.warning("Failed to broadcast gesture.", e);
//            }
    }


    private void onMessage(String message) {
        // Broadcast in response to message
        log.info("Message received: " + message);
        String currentGesture = getService(HandGestureService.class).currentGestureProperty().toString();
        server.broadcast(currentGesture);
        log.info("Gesture Sent");
    }


}
