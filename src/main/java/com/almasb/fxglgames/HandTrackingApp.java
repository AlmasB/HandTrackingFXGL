package com.almasb.fxglgames;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.almasb.fxglgames.tracking.HandGestureEvent;
import com.almasb.fxglgames.tracking.HandGestureService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Optional;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class HandTrackingApp extends GameApplication {

    private Optional<Entity> current = Optional.empty();

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.addEngineService(HandGestureService.class);
    }

    @Override
    protected void initGame() {
        getGameScene().setBackgroundColor(Color.LIGHTGRAY);

        entityBuilder().buildScreenBoundsAndAttach(40);

        spawnNewEntity();

        getService(HandGestureService.class).setOnGesture(event -> {
            if (event.getEventType() == HandGestureEvent.SWIPE_LEFT) {
                current.ifPresent(this::moveLeft);
            } else if (event.getEventType() == HandGestureEvent.SWIPE_RIGHT) {
                current.ifPresent(this::moveRight);
            }
        });
    }

    @Override
    protected void initUI() {
        var text = getUIFactoryService().newText("", Color.WHITE, 22.0);
        text.textProperty().bind(
                new SimpleStringProperty("Wave your hand until tracking is calibrated\n Hand tracking is ready: ")
                        .concat(getService(HandGestureService.class).isReadyProperty())
        );

        addUINode(text, 100, 100);
    }

    private void moveLeft(Entity e) {
        move(e, new Point2D(-1000, 0));
    }

    private void moveRight(Entity e) {
        move(e, new Point2D(1000, 0));
    }

    private void move(Entity e, Point2D velocity) {
        current = Optional.empty();

        e.getComponent(PhysicsComponent.class).getBody().setAwake(true);
        e.getComponent(PhysicsComponent.class).setLinearVelocity(velocity);

        runOnce(this::spawnNewEntity, Duration.seconds(1));
    }

    private void spawnNewEntity() {
        var physics = new PhysicsComponent();
        physics.setFixtureDef(new FixtureDef().density(25.5f).restitution(0.36f));
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setOnPhysicsInitialized(() -> physics.getBody().setAwake(false));

        var e = entityBuilder()
                .at(getAppCenter())
                .bbox(BoundingShape.circle(32))
                .view(texture("ball.png", 64, 64))
                .with(physics)
                .buildAndAttach();

        current = Optional.of(e);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
