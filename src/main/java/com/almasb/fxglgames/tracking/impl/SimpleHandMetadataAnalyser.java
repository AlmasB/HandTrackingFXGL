package com.almasb.fxglgames.tracking.impl;

import com.almasb.fxglgames.tracking.Hand;
import com.almasb.fxglgames.tracking.HandMetadata;
import com.almasb.fxglgames.tracking.HandMetadataAnalyser;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class SimpleHandMetadataAnalyser implements HandMetadataAnalyser {

    @Override
    public HandMetadata analyse(Hand hand) {
        return new HandMetadata(false);
    }
}
