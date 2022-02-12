package com.almasb.fxglgames.tracking;

import java.util.function.Consumer;

/**
 * An abstraction over the hand tracking data producer.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public interface HandTrackingDriver {

    /**
     * Start listening for hand tracking data.
     */
    void start();

    /**
     * Called when there is a new hand tracking data item available from the driver.
     */
    void setOnHandData(Consumer<Hand> dataHandler);

    /**
     * Stop listening for hand tracking data.
     */
    void stop();
}
