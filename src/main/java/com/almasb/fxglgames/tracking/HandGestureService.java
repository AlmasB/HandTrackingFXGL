package com.almasb.fxglgames.tracking;

import com.almasb.fxgl.core.EngineService;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.logging.Logger;
import com.almasb.fxglgames.tracking.gestures.FistGestureEvaluator;
import com.almasb.fxglgames.tracking.gestures.ThumbIndexPinchGestureEvaluator;
import com.almasb.fxglgames.tracking.impl.JSHandTrackingDriver;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import static com.almasb.fxglgames.tracking.HandGesture.*;
import static com.almasb.fxglgames.tracking.HandGesture.NO_HAND;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public final class HandGestureService extends EngineService {

    private static final Logger log = Logger.get(HandGestureService.class);

    private BooleanProperty isReadyProperty = new SimpleBooleanProperty(false);

    private Map<HandGesture, GestureEvaluator> evaluators = new LinkedHashMap<>();

    private ObjectProperty<HandGesture> currentGesture = new SimpleObjectProperty<>(NO_HAND);

    private BlockingQueue<Hand> dataQueue = new ArrayBlockingQueue<>(1000);
    private List<Hand> evalQueue = new ArrayList<>(1000);

    private Consumer<Hand> rawDataHandler = data -> {};

    private EventHandler<HandGestureEvent> handler = event -> {};
    private HandTrackingDriver driver;

    private double minDistanceThreshold = 0.12;

    private int minQueueSize = 10;

    private int noHandCounter = 0;

    @Override
    public void onInit() {
        // populate evaluators
        evaluators.put(THUMB_INDEX_PINCH, new ThumbIndexPinchGestureEvaluator());

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

    public HandGesture getCurrentGesture() {
        return currentGesture.get();
    }

    public ObjectProperty<HandGesture> currentGestureProperty() {
        return currentGesture;
    }

    public void setRawDataHandler(Consumer<Hand> rawDataHandler) {
        this.rawDataHandler = rawDataHandler;
    }

    public Consumer<Hand> getRawDataHandler() {
        return rawDataHandler;
    }

    // this is called on JavaFX thread
    @Override
    public void onUpdate(double tpf) {
        if (dataQueue.isEmpty()) {
            noHandCounter++;

            if (noHandCounter >= 60) {
                currentGesture.set(NO_HAND);
            }

            return;
        }

        noHandCounter = 0;

        try {
            var item = dataQueue.take();

            evaluateGesture(item);

            evalQueue.add(item);

            rawDataHandler.accept(item);
        } catch (InterruptedException e) {
            log.warning("Cannot take item from queue", e);
        }

        evaluateMovement();
    }

    // this is the (static) gesture mapping algorithm, which is currently very basic and requires more thought
    private void evaluateGesture(Hand hand) {
        currentGesture.set(
                evaluators
                .keySet()
                .stream()
                .filter(gesture -> evaluators.get(gesture).evaluate(hand))
                .findFirst()
                .orElse(UNKNOWN)
        );
    }

    // this is the (dynamic) gesture movement mapping algorithm, which is currently very basic and requires more thought
    private void evaluateMovement() {
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
