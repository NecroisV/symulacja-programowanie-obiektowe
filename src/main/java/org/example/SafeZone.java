package org.example;

import java.util.ArrayList;
import java.util.List;

// Strefa bezpieczeństwa - chroni ocalałych, leczy ich i zabrania walk
public class SafeZone {
    private boolean fightingProhibited;
    private float chanceToHeal;           // Szansa na wyleczenie rany w ticku
    private float destructionThreshold;    // Próg zniszczenia (% zniszczonych ścian)
    private int allWallsCount;             // Liczba wszystkich ścian w strefie
    private int destroyedWallsCount;       // Liczba zniszczonych ścian
    private int lifespanTicks;             // Maksymalny czas życia strefy
    private int currentTick = 0;

    private List<Space> coveredSpaces = new ArrayList<>();

    public SafeZone(float chanceToHeal, float destructionThreshold) {
        this.fightingProhibited = true;
        this.chanceToHeal = chanceToHeal;
        this.destructionThreshold = destructionThreshold;
        this.destroyedWallsCount = 0;
        this.lifespanTicks = RNG.nextInt(100, 1500); // Losowy czas życia 100-1500 ticków
    }

    // Dodaje pole do strefy i ustawia referencję zwrotną
    public void addSpace(Space space) {
        coveredSpaces.add(space);
        space.setSafeZone(this);
    }

    // Zlicza wszystkie ściany w strefie (potrzebne do progu zniszczenia)
    public void countWalls() {
        allWallsCount = 0;
        for (Space space : coveredSpaces) {
            if (space.isItWall()) {
                allWallsCount++;
            }
        }
    }

    // Próbuje wyleczyć ocalałego (jeśli strefa nie jest zniszczona)
    public void healSurvivor(Survivor survivor) {
        if (isSafeZoneDestroyed()) return;
        if (RNG.nextFloat() < chanceToHeal) {
            survivor.changeHealthLevel(10);
        }
    }

    // Rejestruje zniszczenie jednej ściany w strefie
    public void commitWallDestruction() {
        destroyedWallsCount++;
        if (isSafeZoneDestroyed()) {
            destroySafeZone();
        }
    }

    // Aktualizuje wiek strefy i sprawdza czy wygasła lub została zniszczona
    public boolean updateAndCheckExpiry() {
        currentTick++;
        if (currentTick >= lifespanTicks) {
            destroySafeZone();
            return true;
        }
        if (isSafeZoneDestroyed()) {
            destroySafeZone();
            return true;
        }
        return false;
    }

    // Sprawdza czy próg zniszczenia został przekroczony
    public boolean isSafeZoneDestroyed() {
        if (allWallsCount == 0) return false;
        return (float) destroyedWallsCount / allWallsCount >= destructionThreshold;
    }

    // Niszczy strefę - czyści referencje w polach
    public void destroySafeZone() {
        for (Space space : coveredSpaces) {
            space.setSafeZone(null);
        }
        coveredSpaces.clear();
    }

    public boolean isFightingProhibited() {
        return fightingProhibited;
    }

    public List<Space> getCoveredSpaces() {
        return coveredSpaces;
    }

    public int getAllWallsCount() {
        return allWallsCount;
    }

    public int getDestroyedWallsCount() {
        return destroyedWallsCount;
    }

    public int getLifespanTicks() {
        return lifespanTicks;
    }

    public int getCurrentTick() {
        return currentTick;
    }
}