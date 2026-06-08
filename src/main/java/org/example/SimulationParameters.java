package org.example;

import java.util.HashMap;
import java.util.Map;

public class SimulationParameters {
    private static SimulationParameters instance;
    private int startingSurvivorAmount = 15;
    private int startingInfectedAmount = 700;
    private Map<String, Integer> moveWeights;
    private int[] startingEqAndWoundChances = new int[]{10, 15}; //EQChance, WouondChance
    private final int simulationSeed = 128;

    private int weaponCount = 80;
    private int clothesCount = 80;
    private int resourceCount = 200;

    private int safeZoneCount = 2;
    private int safeZoneSize = 5;      // wewnętrzny rozmiar (bez ścian)
    private float destructionThreshold = 0.5f;

    private float healChance = 0.25f;
    private float infectionChance = 0.25f;
    private float chanceForWoundAfterBattle = 0.25f;

    private int[] survivorStats = new int[]{100, 20, 5, 3}; //health, strength, FOV, speed
    private int[] infectedStats = new int[]{70, 7, 4, 5}; //health, strength, FOV, speed

    //SEKCJA WYDARZEŃ LOSOWYCH (POGODOWYCH)
    //Odpowiednio: szansa na jakikolwiek event, waga BURZY, waga MGŁY, waga TRZĘSIENIA ZIEMI;
    private final double[] eventChances = new double[]{0.3, 10, 20, 2};
    private final int[] eventDuration = new int[]{1, 5}; //min, max
    private final double earthquakeWallDestroyChance = 0.05;
    private final double[] fogIntensity =  new double[]{0.2, 0.6}; //min, max;
    private final int[] dayNightCycle = new int[]{15, 7}; //długość dnia, długość nocy (w tickach)

    public SimulationParameters() {
        moveWeights = new HashMap<>();
        loadDefaultWeights();
    }

    private void loadDefaultWeights() {
        moveWeights.put("infectedCurrentSeenSurvivor", 200);
        moveWeights.put("infectedMemory", 25);
        moveWeights.put("infectedCloseInfected", 50);

        moveWeights.put("survivorInfected", -100);
        moveWeights.put("survivorSafeZone", 80);
        moveWeights.put("survivorResource", 50);
        moveWeights.put("survivorSurvivor", -125);
        moveWeights.put("survivorEquipment", 150);

    }
    //Ze wzorca projektowego Singleton
    public static SimulationParameters getInstance(){
        if (instance == null){
            instance = new SimulationParameters();
        }
        return instance;
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
    public double[] getFogIntensity(){return fogIntensity;}
    public int[] getDayNightCycle(){return dayNightCycle;}

    public Map<String, Integer> getMoveWeights() {
        return moveWeights;
    }

    public int getWeaponCount() { return weaponCount; }
    public int getClothesCount() { return clothesCount; }
    public int getResourceCount() { return resourceCount; }

    public int[] getSurvivorStats(){return survivorStats;}
    public int[] getInfectedStats(){return infectedStats;}

    public int getSafeZoneCount(){return safeZoneCount;}
    public int getSafeZoneSize(){return safeZoneSize;}
    public float getDestructionThreshold() {return destructionThreshold;}

    public float getHealChance(){return healChance;}
    public float getInfectionChance(){return infectionChance;}
    public float getChanceForWoundAfterBattle(){return chanceForWoundAfterBattle;}
}
