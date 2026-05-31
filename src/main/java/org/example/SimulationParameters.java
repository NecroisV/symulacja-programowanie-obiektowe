package org.example;

import java.util.Map;

public final class SimulationParameters {
    private static SimulationParameters instance;
    private int startingSurvivorAmount = 3;
    private int startingInfectedAmount = 4;
    private float chanceForWoundAfterBattle;
    private Map<String, Float> moveWeights;
    private int[] startingEqAndWoundChances = new int[]{10, 15};
    private final int simulationSeed = 128;
    //Odpowiednio: szansa na jakikolwiek event, waga BURZY, waga MGŁY, waga TRZĘSIENIA ZIEMI;
    private final double[] eventChances = new double[]{0.1, 30, 10, 5};

    public SimulationParameters(){

    }

    //Ze wzorca projektowego Singleton
    public static SimulationParameters getInstance(){
        if (instance == null){
            instance = new SimulationParameters();
        }
        return instance;
    }

    public void loadParameters(){

    }

    public int[] getAgentsAmount(){
        return new int[]{startingSurvivorAmount, startingInfectedAmount};
    }

    public int[] getEqAndWoundChances(){
        return startingEqAndWoundChances;
    }

    public int getSimulationSeed(){
        return simulationSeed;
    }

    public double[] getEventChances(){
        return eventChances;
    }
}
