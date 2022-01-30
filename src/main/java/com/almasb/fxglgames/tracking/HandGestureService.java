package com.almasb.fxglgames.tracking;

import com.almasb.fxgl.core.EngineService;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.logging.Logger;
import com.almasb.fxglgames.tracking.impl.JSHandTrackingDriver;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public final class HandGestureService extends EngineService {

    private static final Logger log = Logger.get(HandGestureService.class);

    private BooleanProperty isReadyProperty = new SimpleBooleanProperty(false);

    private BlockingQueue<Hand> dataQueue = new ArrayBlockingQueue<>(1000);
    private List<Hand> evalQueue = new ArrayList<>(1000);

    private EventHandler<HandGestureEvent> handler = event -> {};
    private HandTrackingDriver driver;

    private double minDistanceThreshold = 0.12;

    private int minQueueSize = 10;

    @Override
    public void onInit() {
        driver = new JSHandTrackingDriver(this::onHandData);

        // starts the driver in a background thread
        driver.start();

        FXGL.getExecutor().schedule(() -> {
            // start the client that connects to the driver
            FXGL.getFXApp().getHostServices().showDocument(Paths.get("hand-tracking.html").toUri().toString());
        }, Duration.seconds(1));
    }

    public double getMinDistanceThreshold() {
        return minDistanceThreshold;
    }

    /**
     * Fluctuations below this value will not be registered.
     */
    public void setMinDistanceThreshold(double minDistanceThreshold) {
        this.minDistanceThreshold = minDistanceThreshold;
    }

    public void setOnGesture(EventHandler<HandGestureEvent> handler) {
        this.handler = handler;
    }

    public BooleanProperty isReadyProperty() {
        return isReadyProperty;
    }

    // this is called on JavaFX thread
    @Override
    public void onUpdate(double tpf) {
        if (dataQueue.isEmpty())
            return;

        try {
            var item = dataQueue.take();

            evalQueue.add(item);
        } catch (InterruptedException e) {
            log.warning("Cannot take item from queue", e);
        }

        evaluate();
    }

    // this is the gesture mapping algorithm, which is currently very basic and requires more thought
    private void evaluate() {
        if (evalQueue.size() < minQueueSize)
            return;

        var itemNew = evalQueue.get(minQueueSize - 1);
        var itemOld = evalQueue.remove(0);

        var itemNewAvg = itemNew.average();
        var itemOldAvg = itemOld.average();

        if (Math.abs(itemNewAvg.getX() - itemOldAvg.getX()) > minDistanceThreshold) {
            if (itemNewAvg.getX() > itemOldAvg.getX()) {

                // gestures are inverted
                handler.handle(new HandGestureEvent(HandGestureEvent.SWIPE_LEFT));
            } else if (itemNewAvg.getX() < itemOldAvg.getX()) {
                handler.handle(new HandGestureEvent(HandGestureEvent.SWIPE_RIGHT));
            }
        }

        isReadyProperty.set(true);
    }

    @Override
    public void onExit() {
        driver.stop();
    }

    // this is called on a bg thread
    private void onHandData(Hand data) {
        try {
            dataQueue.put(data);
        } catch (InterruptedException e) {
            log.warning("Cannot place item in queue", e);
        }
    }
}
