package com.almasb.fxglgames.tracking;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public interface GestureEvaluator {

    /**
     * @return true if the given [hand] is evaluated to be a match for this gesture
     */
    boolean evaluate(Hand hand);
}
