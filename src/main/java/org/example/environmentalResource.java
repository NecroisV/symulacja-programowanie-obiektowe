package org.example;

// Zasób środowiskowy (jedzenie, apteczka) - daje energię i zdrowie po zebraniu
public class environmentalResource {
    private int energyRecovery;      // Ilość odzyskanej energii
    private int healthRecovery;      // Ilość odzyskanego zdrowia
    private int minimalRespawnTime;  // Minimalny czas respawnu po użyciu
    private int timeFromUse;         // Tick od użycia (-1 jeśli nieużyty)
    private boolean isUsed;          // Czy zasób został już użyty

    // Konstruktor - losuje wartości odzysku i czasu respawnu
    public environmentalResource(){
        energyRecovery = RNG.nextInt(20, 30);     // 20-30 energii
        healthRecovery = RNG.nextInt(15, 25);     // 15-25 zdrowia
        minimalRespawnTime = RNG.nextInt(20, 30); // 20-30 ticków respawnu
        timeFromUse = -1;
        isUsed = false;
    }

    // Użycie zasobu - zwraca wartości odzysku i oznacza jako użyty
    public int[] getUsed(){
        isUsed = true;
        timeFromUse = 0;
        return new int[] {energyRecovery, healthRecovery};
    }

    // Sprawdza czy zasób był użyty
    public boolean wasUsed(){
        return isUsed;
    }

    // Aktualizuje czas od użycia (wywoływane co tick)
    public void updateTime(){
        timeFromUse += 1;
    }

    // Próbuje respawnąć zasób - gdy czas od użycia >= minimalRespawnTime
    public void Respawn(){
        if(RNG.nextInt(minimalRespawnTime)+timeFromUse>minimalRespawnTime){
            isUsed = false;
        }
    }
}