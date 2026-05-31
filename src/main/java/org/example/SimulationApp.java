package org.example;

import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SimulationApp extends Application {

    private static final int BOARD_WIDTH = 300;
    private static final int BOARD_HEIGHT = 150;
    private static final double TILE_SIZE = 8.0;
    private static final double LOGS_HEIGHT = 160.0;

    private SimulationEnvironment environment;

    @Override
    public void start(Stage primaryStage) {
        environment = new SimulationEnvironment(BOARD_WIDTH, BOARD_HEIGHT);
        RNG.initRNG(SimulationParameters.getInstance().getSimulationSeed());

        double canvasWidth = BOARD_WIDTH * TILE_SIZE;
        double canvasHeight = (BOARD_HEIGHT * TILE_SIZE) + LOGS_HEIGHT;

        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, canvasWidth, canvasHeight);

        new AnimationTimer() {
            private long lastUpdate = 0;

            private final long TICK_RATE = 800000L;

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