package com.almasb.fxglgames.tracking;

import com.almasb.fxgl.core.EngineService;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.logging.Logger;
import com.almasb.fxglgames.tracking.gestures.GeometricGestureEvaluator;
import com.almasb.fxglgames.tracking.impl.JSHandTrackingDriver;
import com.almasb.fxglgames.tracking.impl.SimpleHandMetadataAnalyser;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;

import static com.almasb.fxglgames.tracking.HandGesture.NO_HAND;
import static com.almasb.fxglgames.tracking.HandGesture.UNKNOWN;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public final class HandGestureService extends EngineService {

    private static final Logger log = Logger.get(HandGestureService.class);

    private BooleanProperty isReadyProperty = new SimpleBooleanProperty(false);

    // TODO: allow setting
    private GestureEvaluator gestureEvaluator = new GeometricGestureEvaluator();

    private ObjectProperty<HandGesture> currentGesture = new SimpleObjectProperty<>(NO_HAND);

    private ObjectProperty<HandOrientation> currentOrientation = new SimpleObjectProperty<>(HandOrientation.UP);

    public BooleanProperty ringFingerDown = new SimpleBooleanProperty(false);

    public BooleanProperty pinkyDown = new SimpleBooleanProperty(false);

    public BooleanProperty palmForwards = new SimpleBooleanProperty(false);

    public BooleanProperty thumbCurled = new SimpleBooleanProperty(false);

    private BlockingQueue<Hand> dataQueue = new ArrayBlockingQueue<>(1000);
    private List<Hand> evalQueue = new ArrayList<>(1000);

    private BiConsumer<Hand, HandMetadataAnalyser> rawDataHandler = (data, analyser) -> {};

    private EventHandler<HandGestureEvent> handler = event -> {};
    private HandTrackingDriver driver;

    private double minDistanceThreshold = 0.12;

    private int minQueueSize = 10;

    private int noHandCounter = 0;

    public double ringMCPY;
    public double ringTipY;

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

    public HandGesture getCurrentGesture() {
        return currentGesture.get();
    }

    public ObjectProperty<HandGesture> currentGestureProperty() {
        return currentGesture;
    }

    public BooleanProperty getThumbCurled() { return thumbCurled; }

    public BooleanProperty getRingFingerDown() { return ringFingerDown; }

    public BooleanProperty getPinkyDown() { return pinkyDown; }

    public HandOrientation getCurrentOrientation() {return currentOrientation.get();}

    public ObjectProperty<HandOrientation> currentOrientationProperty(){
        return currentOrientation;
    }

    public BooleanProperty palmForwardsProperty() { return palmForwards; }

    public void setRawDataHandler(BiConsumer<Hand, HandMetadataAnalyser> rawDataHandler) {
        this.rawDataHandler = rawDataHandler;
    }

    public BiConsumer<Hand, HandMetadataAnalyser> getRawDataHandler() {
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

            var analyser = new SimpleHandMetadataAnalyser();

            evaluateGesture(item, analyser);

            evalQueue.add(item);

            rawDataHandler.accept(item, analyser);

            currentOrientation.set(GeometricGestureEvaluator.getOrientation(item));

            ringTipY = item.getPoint(HandLandmark.RING_FINGER_TIP).getY();
            ringMCPY = item.getPoint(HandLandmark.RING_FINGER_MCP).getY();

            ringFingerDown.set(GeometricGestureEvaluator.isFingerDown(item, HandLandmark.RING_FINGER_TIP));

            pinkyDown.set(GeometricGestureEvaluator.isFingerDown(item, HandLandmark.PINKY_TIP));

            thumbCurled.set(GeometricGestureEvaluator.isFingerDown(item, HandLandmark.THUMB_TIP));

            palmForwards.set(GeometricGestureEvaluator.getPalmFacingFowards(item));

        } catch (InterruptedException e) {
            log.warning("Cannot take item from queue", e);
        }

        evaluateMovement();


    }

    // this is the (static) gesture mapping algorithm, which is currently very basic and requires more thought
    private void evaluateGesture(Hand hand, HandMetadataAnalyser analyser) {
        var result = gestureEvaluator.evaluate(hand, analyser);

        currentGesture.set(
            result.output()
                    .entrySet()
                    .stream()
                    .max(Comparator.comparingDouble(Map.Entry::getValue))
                    // TODO: threshold ...
                    .map(entry -> entry.getValue() > 0.02 ? entry.getKey() : UNKNOWN)
                    .get()
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
