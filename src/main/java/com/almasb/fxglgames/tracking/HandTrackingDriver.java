package com.almasb.fxglgames.tracking;

import java.util.function.Consumer;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public interface HandTrackingDriver {

    void start();
    void setOnHandData(Consumer<Hand> dataHandler);
    void stop();
}
