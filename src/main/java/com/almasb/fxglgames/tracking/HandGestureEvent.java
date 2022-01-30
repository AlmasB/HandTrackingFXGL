package com.almasb.fxglgames.tracking;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class HandGestureEvent extends Event {

    public static final EventType<HandGestureEvent> ANY = new EventType<>(Event.ANY, "HAND_GESTURE");

    public static final EventType<HandGestureEvent> SWIPE_LEFT = new EventType<>(ANY, "SWIPE_LEFT");
    public static final EventType<HandGestureEvent> SWIPE_RIGHT = new EventType<>(ANY, "SWIPE_RIGHT");

    public HandGestureEvent(EventType<? extends HandGestureEvent> eventType) {
        super(eventType);
    }
}
