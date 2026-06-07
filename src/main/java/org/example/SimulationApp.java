package org.example;

import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SimulationApp extends Application {

    private static final int BOARD_WIDTH = 700;
    private static final int BOARD_HEIGHT = 500;
    private static final double TILE_SIZE = 2.0;
    private static final double LOGS_HEIGHT = 200.0;
    private static SimulationEnvironment environment;

    public static SimulationEnvironment getEnvironment() {
        if(environment == null){
            environment = new SimulationEnvironment(BOARD_WIDTH, BOARD_HEIGHT);
        }
        return environment;
    }

    @Override
    public void start(Stage primaryStage) {
        environment = getEnvironment();

        double canvasWidth = BOARD_WIDTH * TILE_SIZE;
        double canvasHeight = (BOARD_HEIGHT * TILE_SIZE) + LOGS_HEIGHT;

        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, canvasWidth, canvasHeight);

        new AnimationTimer() {
            private long lastUpdate = 0;

            private final long TICK_RATE = 8000000L;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= TICK_RATE) {
                    environment.simulationStep(gc, TILE_SIZE);
                    lastUpdate = now;
                }
            }
        }.start();

        primaryStage.setTitle("Symulacja");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}