package org.example;

import java.util.concurrent.TimeUnit;
public class Main {
    public static void main(String[] args) {
        SimulationEnvironment simulationEnvironment = new SimulationEnvironment(10, 15);

        for(int i = 0; i <= 250; i++) {
            simulationEnvironment.simulationStep();
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}