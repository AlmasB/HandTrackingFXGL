package com.almasb.fxglgames.tracking.gestures;

import com.almasb.fxglgames.tracking.*;
import javafx.util.Pair;

import java.util.EnumMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.almasb.fxglgames.tracking.HandGesture.*;
import static com.almasb.fxglgames.tracking.HandLandmark.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class GeometricGestureEvaluator implements GestureEvaluator {

    private EnumMap<HandGesture, BiFunction<Hand, HandMetadata, Double>> evaluators = new EnumMap<>(HandGesture.class);

    public GeometricGestureEvaluator() {
        // TODO: populate evaluators
        evaluators.put(THUMB_INDEX_PINCH, this::evalThumbIndexPinch);
        evaluators.put(THUMB_PINKY_PINCH, this::evalThumbPinkyPinch);
        evaluators.put(THUMB_RING_FINGER_PINCH, this::evalThumbRingPinch);
        evaluators.put(THUMB_MIDDLE_FINGER_PINCH, this::evalThumbMiddlePinch);
    }

    @Override
    public GestureEvaluationResult evaluate(Hand hand, HandMetadataAnalyser analyser) {
        var metadata = analyser.analyse(hand);

        var map = evaluators.entrySet()
                .stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue().apply(hand, metadata)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        return new GestureEvaluationResult(map);
    }

    // TODO: take into account z
    // TODO: allow setting thresholds
    private double evalThumbIndexPinch(Hand hand, HandMetadata metadata) {
        // TODO: double range map
        return hand.getPoint(THUMB_TIP).distance(hand.getPoint(INDEX_FINGER_TIP)) < 0.05 ? 1.0 : 0.0;
    }

    private double evalThumbPinkyPinch(Hand hand, HandMetadata metadata) {
        // TODO: double range map
        return hand.getPoint(THUMB_TIP).distance(hand.getPoint(PINKY_TIP)) < 0.05 ? 1.0 : 0.0;
    }

    private double evalThumbMiddlePinch(Hand hand, HandMetadata metadata) {
        // TODO: double range map
        return hand.getPoint(THUMB_TIP).distance(hand.getPoint(MIDDLE_FINGER_TIP)) < 0.05 ? 1.0 : 0.0;
    }

    private double evalThumbRingPinch(Hand hand, HandMetadata metadata) {
        // TODO: double range map
        return hand.getPoint(THUMB_TIP).distance(hand.getPoint(RING_FINGER_TIP)) < 0.05 ? 1.0 : 0.0;
    }
}
