package org.example;

import java.util.HashMap;
import java.util.Map;

public class SimulationParameters {
    private static SimulationParameters instance;
    private static int selectedProfile = 1; // Domyślnie ładowany profil 1

    private int startingSurvivorAmount;
    private int startingInfectedAmount;
    private Map<String, Integer> moveWeights;
    private int[] startingEqAndWoundChances = new int[]{10, 15}; // EQChance, WoundChance
    private int simulationSeed = 128;

    private int weaponCount;
    private int clothesCount;
    private int resourceCount;

    private int safeZoneCount;
    private int safeZoneSize = 5;      // wewnętrzny rozmiar (bez ścian)
    private float destructionThreshold = 0.5f;

    private float healChance;
    private float infectionChance;
    private float chanceForWoundAfterBattle;

    private int[] survivorStats = new int[]{100, 20, 5, 3}; // health, strength, FOV, speed
    private int[] infectedStats = new int[]{70, 7, 4, 5};   // health, strength, FOV, speed

    private double[] eventChances;
    private final int[] eventDuration = new int[]{1, 5}; // min, max
    private final double earthquakeWallDestroyChance = 0.05;
    private final double[] fogIntensity =  new double[]{0.2, 0.6}; // min, max
    private final int[] dayNightCycle = new int[]{15, 7}; // długość dnia, długość nocy

    private SimulationParameters() {
        moveWeights = new HashMap<>();
        loadDefaultWeights();
    }

    public static void setProfile(int profileNumber) {
        if (profileNumber < 1 || profileNumber > 3) {
            throw new IllegalArgumentException("Dostępne profile to: 1, 2 lub 3");
        }
        selectedProfile = profileNumber;
        if (instance != null) {
            instance.loadProfile(selectedProfile);
        }
    }

    public void setSeed(int seedNumber){
        simulationSeed = Math.abs(seedNumber);
    }

    public static SimulationParameters getInstance(){
        if (instance == null){
            instance = new SimulationParameters();
            instance.loadProfile(selectedProfile);
        }
        return instance;
    }

    private void loadProfile(int profileNumber) {
        switch (profileNumber) {
            case 1:
                this.startingSurvivorAmount = 25;
                this.startingInfectedAmount = 500;
                this.weaponCount = 30;
                this.clothesCount = 30;
                this.resourceCount = 100;
                this.safeZoneCount = 1;
                this.healChance = 0.25f;
                this.infectionChance = 0.10f;
                this.chanceForWoundAfterBattle = 0.20f;
                this.eventChances = new double[]{0.25, 10, 20, 2};
                break;

            case 2:
                this.startingSurvivorAmount = 40;
                this.startingInfectedAmount = 200;
                this.weaponCount = 200;
                this.clothesCount = 200;
                this.resourceCount = 400;
                this.safeZoneCount = 3;
                this.healChance = 0.50f;
                this.infectionChance = 0.15f;
                this.chanceForWoundAfterBattle = 0.20f;
                this.eventChances = new double[]{0.30, 10, 20, 2};
                break;

            case 3:
                this.startingSurvivorAmount = 25;
                this.startingInfectedAmount = 450;
                this.weaponCount = 100;
                this.clothesCount = 100;
                this.resourceCount = 180;
                this.safeZoneCount = 2;
                this.healChance = 0.35f;
                this.infectionChance = 0.15f;
                this.chanceForWoundAfterBattle = 0.25f;
                this.eventChances = new double[]{0.60, 20, 40, 4};
                break;
        }
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

    public int[] getAgentsAmount() { return new int[]{startingSurvivorAmount, startingInfectedAmount}; }
    public int[] getEqAndWoundChances() { return startingEqAndWoundChances; }
    public int getSimulationSeed(){ return simulationSeed; }

    public double[] getEventChances(){ return eventChances; }
    public int[] getEventDuration(){ return eventDuration; }
    public double getEarthquakeWallDestroyChance(){ return earthquakeWallDestroyChance; }
    public double[] getFogIntensity(){ return fogIntensity; }
    public int[] getDayNightCycle(){ return dayNightCycle; }

    public Map<String, Integer> getMoveWeights() { return moveWeights; }

    public int getWeaponCount() { return weaponCount; }
    public int getClothesCount() { return clothesCount; }
    public int getResourceCount() { return resourceCount; }

    public int[] getSurvivorStats(){ return survivorStats; }
    public int[] getInfectedStats(){ return infectedStats; }

    public int getSafeZoneCount(){ return safeZoneCount; }
    public int getSafeZoneSize(){ return safeZoneSize; }
    public float getDestructionThreshold() { return destructionThreshold; }

    public float getHealChance(){ return healChance; }
    public float getInfectionChance(){ return infectionChance; }
    public float getChanceForWoundAfterBattle(){ return chanceForWoundAfterBattle; }
}