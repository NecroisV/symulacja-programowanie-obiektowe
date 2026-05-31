package org.example;

import java.util.HashMap;
import java.util.Map;

public class SimulationParameters {
    private int startingSurvivorAmount = 50;
    private int startingInfectedAmount = 500;
    private float chanceForWoundAfterBattle;

    private Map<String, Integer> moveWeights;

    private int[] startingEqAndWoundChances = new int[]{10, 15};

    public SimulationParameters() {
        moveWeights = new HashMap<>();
        loadDefaultWeights();
    }

    private void loadDefaultWeights() {
        moveWeights.put("infectedCurrentSeenSurvivor", 100);
        moveWeights.put("infectedMemory", 25);
        moveWeights.put("infectedCloseInfected", 5);

        moveWeights.put("survivorInfected", -100);
        moveWeights.put("survivorSafeZone", 80);
        moveWeights.put("survivorWeapon", 50);
        moveWeights.put("survivorResource", 50);
        moveWeights.put("survivorSurvivor", -125);
        moveWeights.put("survivorClothes", 50);

    }

    public void loadParameters() {
    }

    public int[] getAgentsAmount() {
        return new int[]{startingSurvivorAmount, startingInfectedAmount};
    }

    public int[] getEqAndWoundChances() {
        return startingEqAndWoundChances;
    }

    public Map<String, Integer> getMoveWeights() {
        return moveWeights;
    }
}