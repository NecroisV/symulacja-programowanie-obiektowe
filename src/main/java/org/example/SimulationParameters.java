package org.example;

import java.util.Map;

public class SimulationParameters {
    private int startingSurvivorAmount = 3;
    private int startingInfectedAmount = 4;
    private float chanceForWoundAfterBattle;
    private Map<String, Float> moveWeights;
    private int[] startingEqAndWoundChances = new int[]{10, 15};

    public SimulationParameters(){

    }

    public void loadParameters(){

    }

    public int[] getAgentsAmount(){
        return new int[]{startingSurvivorAmount, startingInfectedAmount};
    }

    public int[] getEqAndWoundChances(){
        return startingEqAndWoundChances;
    }
}
