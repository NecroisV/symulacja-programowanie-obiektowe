package org.example;

public class Main {
    static void main(String[] args) {
        RNG.initRNG(SimulationParameters.getInstance().getSimulationSeed());
        SimulationApp.main(args);
    }
}