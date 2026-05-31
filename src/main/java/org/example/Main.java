package org.example;

public class Main {
    public static void main(String[] args) {
        RNG.initRNG(SimulationParameters.getInstance().getSimulationSeed());
        SimulationApp.main(args);
    }
}