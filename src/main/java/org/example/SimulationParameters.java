package org.example;

import java.util.HashMap;
import java.util.Map;

// Klasa przechowująca wszystkie parametry symulacji (wzorzec Singleton)
public class SimulationParameters {
    private static SimulationParameters instance;
    private static int selectedProfile = 1; // Domyślnie ładowany profil 1

    // Podstawowe parametry populacji
    private int startingSurvivorAmount;
    private int startingInfectedAmount;
    private Map<String, Integer> moveWeights;     // Wagi decyzyjne dla agentów
    private int[] startingEqAndWoundChances = new int[]{10, 15}; // EQChance, WoundChance
    private int simulationSeed = 128;

    // Parametry ekwipunku i zasobów
    private int weaponCount;
    private int clothesCount;
    private int resourceCount;

    // Parametry stref bezpieczeństwa
    private int safeZoneCount;
    private int safeZoneSize = 5;      // wewnętrzny rozmiar (bez ścian)
    private float destructionThreshold = 0.5f; // Próg zniszczenia (50% ścian)

    // Szanse dla mechanik gry
    private float healChance;           // Szansa na wyleczenie rany
    private float infectionChance;      // Szansa na infekcję po walce
    private float chanceForWoundAfterBattle; // Szansa na ranę po walce

    // Statystyki początkowe agentów
    private int[] survivorStats = new int[]{100, 20, 5, 3}; // health, strength, FOV, speed
    private int[] infectedStats = new int[]{70, 7, 4, 5};   // health, strength, FOV, speed

    // Parametry zdarzeń losowych
    private double[] eventChances;       // [szansa_spawn, waga_burzy, waga_mgly, waga_trzesienia]
    private final int[] eventDuration = new int[]{1, 5}; // min, max czas trwania zdarzenia
    private final double earthquakeWallDestroyChance = 0.05; // 5% szansy na zniszczenie ściany
    private final double[] fogIntensity = new double[]{0.2, 0.6}; // min, max poziom mgły
    private final int[] dayNightCycle = new int[]{15, 7}; // długość dnia, długość nocy (ticki)

    // Prywatny konstruktor (Singleton)
    private SimulationParameters() {
        moveWeights = new HashMap<>();
        loadDefaultWeights();
    }

    // Ustawia aktywny profil symulacji (1-3)
    public static void setProfile(int profileNumber) {
        if (profileNumber < 1 || profileNumber > 3) {
            throw new IllegalArgumentException("Dostępne profile to: 1, 2 lub 3");
        }
        selectedProfile = profileNumber;
        if (instance != null) {
            instance.loadProfile(selectedProfile);
        }
    }

    // Ustawia seed dla generatora liczb losowych
    public void setSeed(int seedNumber){
        simulationSeed = Math.abs(seedNumber);
    }

    // Zwraca instancję Singleton
    public static SimulationParameters getInstance(){
        if (instance == null){
            instance = new SimulationParameters();
            instance.loadProfile(selectedProfile);
        }
        return instance;
    }

    // Ładuje profil o podanym numerze
    private void loadProfile(int profileNumber) {
        switch (profileNumber) {
            case 1: // Profil 1 - trudny (mało ocalałych, dużo zakażonych)
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

            case 2: // Profil 2 - łatwy (dużo zasobów, mało zakażonych)
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

            case 3: // Profil 3 - średni (zbalansowany)
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

    // Ładuje domyślne wagi decyzyjne dla agentów
    private void loadDefaultWeights() {
        // Wagi dla zakażonych
        moveWeights.put("infectedCurrentSeenSurvivor", 200); // Widziany ocalały
        moveWeights.put("infectedMemory", 25);               // Pamięć o ocalałym
        moveWeights.put("infectedCloseInfected", 50);        // Inny zakażony w pobliżu

        // Wagi dla ocalałych
        moveWeights.put("survivorInfected", -100);    // Unikanie zakażonych
        moveWeights.put("survivorSafeZone", 80);      // Dążenie do strefy bezpieczeństwa
        moveWeights.put("survivorResource", 50);      // Dążenie do zasobów
        moveWeights.put("survivorSurvivor", -125);    // Unikanie innych ocalałych
        moveWeights.put("survivorEquipment", 150);    // Dążenie do ekwipunku
    }

    //Gettery dla agentow
    public int[] getAgentsAmount() { return new int[]{startingSurvivorAmount, startingInfectedAmount}; }
    public int[] getEqAndWoundChances() { return startingEqAndWoundChances; }
    public int getSimulationSeed(){ return simulationSeed; }

    //Getter dla eventów i pory dnia
    public double[] getEventChances(){ return eventChances; }
    public int[] getEventDuration(){ return eventDuration; }
    public double getEarthquakeWallDestroyChance(){ return earthquakeWallDestroyChance; }
    public double[] getFogIntensity(){ return fogIntensity; }
    public int[] getDayNightCycle(){ return dayNightCycle; }

    //Getter dla poruszania się
    public Map<String, Integer> getMoveWeights() { return moveWeights; }


    //Gettery dla ilości ekwipunku i zasobów
    public int getWeaponCount() { return weaponCount; }
    public int getClothesCount() { return clothesCount; }
    public int getResourceCount() { return resourceCount; }

    //Gettery dla statystyk agentów
    public int[] getSurvivorStats(){ return survivorStats; }
    public int[] getInfectedStats(){ return infectedStats; }

    //Getterty dla parametrów safe-zone
    public int getSafeZoneCount(){ return safeZoneCount; }
    public int getSafeZoneSize(){ return safeZoneSize; }
    public float getDestructionThreshold() { return destructionThreshold; }

    //Gettery dla parametrów walki
    public float getHealChance(){ return healChance; }
    public float getInfectionChance(){ return infectionChance; }
    public float getChanceForWoundAfterBattle(){ return chanceForWoundAfterBattle; }
}