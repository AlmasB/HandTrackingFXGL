package com.almasb.fxglgames.tracking.impl;

import com.almasb.fxgl.logging.Logger;
import com.almasb.fxglgames.tracking.Hand;
import com.almasb.fxglgames.tracking.HandTrackingDriver;
import javafx.geometry.Point3D;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public final class JSHandTrackingDriver implements HandTrackingDriver {

    private Consumer<Hand> dataHandler;

    private MediaPipeServer server = new MediaPipeServer(
            new InetSocketAddress("localhost", 55555),
            this::onMessage
    );

    public JSHandTrackingDriver(Consumer<Hand> dataHandler) {
        this.dataHandler = dataHandler;
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void setOnHandData(Consumer<Hand> dataHandler) {
        this.dataHandler = dataHandler;
    }

    @Override
    public void stop() {
        try {
            server.stop();
        } catch (InterruptedException e) {
            Logger.get(JSHandTrackingDriver.class).warning("Failed to stop server.", e);
        }
    }

    private void onMessage(String message) {
        try {
            var rawData = message.split(",");

            int id = Integer.parseInt(rawData[0]);

            var points = new ArrayList<Point3D>();

            for (int i = 1; i < rawData.length; i += 3) {
                var x = Double.parseDouble(rawData[i + 0]);
                var y = Double.parseDouble(rawData[i + 1]);
                var z = Double.parseDouble(rawData[i + 2]);

                points.add(new Point3D(x, y, z));
            }

            dataHandler.accept(new Hand(id, points));
        } catch (Exception e) {
            Logger.get(JSHandTrackingDriver.class).warning("Failed to parse message.", e);
        }
    }
}
