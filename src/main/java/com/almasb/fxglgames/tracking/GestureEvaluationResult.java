package com.almasb.fxglgames.tracking;

import java.util.Map;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public record GestureEvaluationResult(

        // Value range in [0..1], 0 - 0%, 1 - 100%.
        Map<HandGesture, Double> output
) { }
