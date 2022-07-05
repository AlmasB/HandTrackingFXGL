package com.almasb.fxglgames;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.almasb.fxglgames.socket.PythonSocketService;
import com.almasb.fxglgames.tracking.Hand;
import com.almasb.fxglgames.tracking.HandGestureService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * TODO: this is only for 1-handed tracking.
 * TODO: ask for user calibration?
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class HandTrackingApp extends GameApplication {

    private Optional<Entity> current = Optional.empty();

    private GraphicsContext g;

    private Text debugText;

    private boolean isDrawing = false;
    private Circle pointer;

    private double oldX = -1;
    private double oldY = -1;

    private Text ringMCP;
    private Text ringTip;

    private Text thumbCurledText;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setManualResizeEnabled(true);
        settings.addEngineService(HandGestureService.class);
        settings.addEngineService(PythonSocketService.class);
    }

    @Override
    protected void initGame() {
        pointer = new Circle(15, 15, 15, Color.RED);
        pointer.setStroke(Color.YELLOW);

        addUINode(pointer);

        debugText = new Text("");
        debugText.setFont(Font.font(22));

        //addUINode(debugText, 50, 250);

        getGameScene().setBackgroundColor(Color.LIGHTGRAY);

        entityBuilder().buildScreenBoundsAndAttach(40);

        spawnNewEntity();

        var canvas = new Canvas(getAppWidth(), getAppHeight());
        g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLUE);
        g.setStroke(Color.RED);
        g.setLineWidth(6);

        addUINode(canvas);


        getService(HandGestureService.class).setRawDataHandler((hand, analyser) -> {
            var dist = hand.points().get(4).distance(hand.points().get(8));

            debugText.setText(String.format("distance: %.3f", dist));
            drawHand(hand);

            var indexFingerTip = hand.points().get(8);

            pointer.setTranslateX((1 - indexFingerTip.getX()) * 1280);
            pointer.setTranslateY(indexFingerTip.getY() * 720);
        });



        getService(HandGestureService.class).currentGestureProperty().addListener((o, old, gesture) -> {
//            if (gesture == HandGesture.THUMB_INDEX_PINCH) {
//                isDrawing = true;
//            } else {
//                isDrawing = false;
//            }
        });

//        getService(HandGestureService.class).setOnGesture(event -> {
//            if (event.getEventType() == HandGestureEvent.SWIPE_LEFT) {
//                current.ifPresent(this::moveLeft);
//            } else if (event.getEventType() == HandGestureEvent.SWIPE_RIGHT) {
//                current.ifPresent(this::moveRight);
//            }
//        });
    }

    @Override
    protected void initUI() {
        var text = getUIFactoryService().newText("", Color.WHITE, 22.0);
        text.textProperty().bind(
                new SimpleStringProperty("Wave your hand until tracking is calibrated\n Hand tracking is ready: ")
                        .concat(getService(HandGestureService.class).isReadyProperty())
                        .concat("\n")
                        .concat("Current gesture: ")
                        .concat(getService(HandGestureService.class).currentGestureProperty())
                        .concat("\n")
                        .concat("Is ring finger curled: ")
                        .concat(getService(HandGestureService.class).getRingFingerDown())
                        .concat("\n")
                        .concat("Is pinky curled: ")
                        .concat(getService(HandGestureService.class).getPinkyDown())
                        .concat("\n")
                        .concat("Palm Facing Forwards: ")
                        .concat(getService(HandGestureService.class).palmForwardsProperty())
                        .concat("\n")
                        .concat("Current Orientation: ")
                        .concat(getService(HandGestureService.class).currentOrientationProperty())
        );

        addUINode(text, 50, 50);

//        var indexText = getUIFactoryService().newText("", Color.WHITE, 22.0);
//        text.textProperty().bind(
//                new SimpleStringProperty("Index Finger is Down: ")
//                        .concat(getService(HandGestureService.class).indexFingerDownProperty())
//
//        );

//        var orientationText = getUIFactoryService().newText("", Color.BLUE, 22.0);
//        orientationText.textProperty().bind(
//                new SimpleStringProperty("Hand Orientation: ")
//                        .concat(getService(HandGestureService.class).currentOrientationProperty())
//        );
//
//        addUINode(orientationText, 150, 150);
//
//        ringMCP = new Text();
//        addUINode(ringMCP, 500, 800);
//        ringTip = new Text();
//        addUINode( ringTip, 500, 500);
//
//        addUINode(indexText, 500, 300);

        thumbCurledText = new Text();
        addUINode(thumbCurledText, 500, 500);


    }

    private void drawHand(Hand hand) {
        g.clearRect(0, 0, getAppWidth(), getAppHeight());

        List<Point3D> points = hand.points();
        for (int i = 0; i < points.size(); i++) {
            Point3D p = points.get(i);
            var x = (1 - p.getX()) * 1280;
            var y = p.getY() * 720;

            g.fillOval(x, y, 15, 15);

            g.fillText(Integer.toString(i), x, y);
        }

        drawLine(hand.points(), 0, 5);
        drawLine(hand.points(), 5, 9);
        drawLine(hand.points(), 9, 13);
        drawLine(hand.points(), 13, 17);
        drawLine(hand.points(), 17, 0);

//        ringMCP.setText("Ring MCP: " + String.valueOf(getService(HandGestureService.class).ringMCPY));
//        ringTip.setText("Ring Tip: " + String.valueOf(getService(HandGestureService.class).ringTipY));
        thumbCurledText.setText("Thumb Curled: " + String.valueOf(getService(HandGestureService.class).getThumbCurled()));
    }

    private void drawLine(List<Point3D> points, int index0, int index1) {
        g.strokeLine(
                (1 - points.get(index0).getX()) * 1280, points.get(index0).getY() * 720,
                (1 - points.get(index1).getX()) * 1280, points.get(index1).getY() * 720
        );
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
                //.view(texture("ball.png", 64, 64))
                .with(physics)
                .buildAndAttach();

        current = Optional.of(e);
    }

    @Override
    protected void onUpdate(double tpf) {
        if (!isDrawing) {
            oldX = -1;
            oldY = -1;
            return;
        }

        //g.fillOval(pointer.getTranslateX(), pointer.getTranslateY(), 10, 10);

        if (oldX != -1 && oldY != -1) {
            g.strokeLine(oldX, oldY, pointer.getTranslateX(), pointer.getTranslateY());
        }

        oldX = pointer.getTranslateX();
        oldY = pointer.getTranslateY();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
