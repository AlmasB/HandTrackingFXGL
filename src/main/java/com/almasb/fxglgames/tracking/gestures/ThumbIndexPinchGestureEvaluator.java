package com.almasb.fxglgames.tracking.gestures;

import com.almasb.fxglgames.tracking.GestureEvaluator;
import com.almasb.fxglgames.tracking.Hand;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class ThumbIndexPinchGestureEvaluator implements GestureEvaluator {
    @Override
    public boolean evaluate(Hand hand) {
        // TODO: extract into enum
        // TODO: take into account z
        // TODO: allow setting thresholds

        return hand.points().get(4).distance(hand.points().get(8)) < 0.05;
    }
}
