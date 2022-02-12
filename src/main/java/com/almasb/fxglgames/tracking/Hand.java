package com.almasb.fxglgames.tracking;

import javafx.geometry.Point3D;

import java.util.List;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public record Hand(
        int id,

        // list of 21 hand landmarks: https://google.github.io/mediapipe/solutions/hands#hand-landmark-model
        List<Point3D> points
) {

    public Point3D getPoint(HandLandmark landmark) {
        return points.get(landmark.ordinal());
    }

    public Point3D average() {
        return points
                .stream()
                .reduce(Point3D.ZERO, Point3D::add)
                .multiply(1.0 / points.size());
    }
}
