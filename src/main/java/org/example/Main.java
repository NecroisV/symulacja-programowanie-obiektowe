package org.example;

// Klasa główna - inicjalizuje RNG i uruchamia aplikację JavaFX
public class Main {
    static void main(String[] args) {
        RNG.initRNG(SimulationParameters.getInstance().getSimulationSeed());
        SimulationApp.main(args);
    }
}