package org.example;

import java.util.HashMap;
import java.util.Map;

public class SimulationParameters {
    private static SimulationParameters instance;
    private int startingSurvivorAmount = 1500;
    private int startingInfectedAmount = 5000;
    private float chanceForWoundAfterBattle;
    private Map<String, Integer> moveWeights;
    private int[] startingEqAndWoundChances = new int[]{10, 15};
    private final int simulationSeed = 128;
    private int weaponCount = 800;
    private int clothesCount = 800;

    //SEKCJA WYDARZEŃ LOSOWYCH (POGODOWYCH)
    //Odpowiednio: szansa na jakikolwiek event, waga BURZY, waga MGŁY, waga TRZĘSIENIA ZIEMI;
    private final double[] eventChances = new double[]{0.3, 10, 0, 2};
    private final int[] eventDuration = new int[]{1, 5}; //min, max
    private final double earthquakeWallDestroyChance = 0.04;

    public SimulationParameters() {
        moveWeights = new HashMap<>();
        loadDefaultWeights();
    }

    private void loadDefaultWeights() {
        moveWeights.put("infectedCurrentSeenSurvivor", 100);
        moveWeights.put("infectedMemory", 25);
        moveWeights.put("infectedCloseInfected", 15);

        moveWeights.put("survivorInfected", -100);
        moveWeights.put("survivorSafeZone", 80);
        moveWeights.put("survivorResource", 50);
        moveWeights.put("survivorSurvivor", -125);
        moveWeights.put("survivorEquipment", 50);

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

    public int[] getAgentsAmount() {
        return new int[]{startingSurvivorAmount, startingInfectedAmount};
    }

    public int[] getEqAndWoundChances() {
        return startingEqAndWoundChances;
    }

    public int getSimulationSeed(){return simulationSeed;}

    public double[] getEventChances(){return eventChances;}
    public int[] getEventDuration(){return eventDuration;}
    public double getEarthquakeWallDestroyChance(){return earthquakeWallDestroyChance;}

    public Map<String, Integer> getMoveWeights() {
        return moveWeights;
    }
    public int getWeaponCount() { return weaponCount; }
    public int getClothesCount() { return clothesCount; }
}
