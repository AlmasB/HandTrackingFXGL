package com.almasb.fxglgames.tracking;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public interface GestureEvaluator {

    GestureEvaluationResult evaluate(Hand hand, HandMetadataAnalyser analyser);
}
