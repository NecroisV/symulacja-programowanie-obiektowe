package org.example;

import java.util.ArrayList;
import java.util.List;

public class SafeZone {
    private boolean fightingProhibited;
    private float chanceToHeal;
    private float destructionThreshold;
    private int allWallsCount;
    private int destroyedWallsCount;
    private int lifespanTicks;
    private int currentTick = 0;

    private List<Space> coveredSpaces = new ArrayList<>();

    public SafeZone(float chanceToHeal, float destructionThreshold) {
        this.fightingProhibited = true;
        this.chanceToHeal = chanceToHeal;
        this.destructionThreshold = destructionThreshold;
        this.destroyedWallsCount = 0;
        this.lifespanTicks = RNG.nextInt(100, 1500);
    }

    public void addSpace(Space space) {
        coveredSpaces.add(space);
        space.setSafeZone(this);
    }

    public void countWalls() {
        allWallsCount = 0;
        for (Space space : coveredSpaces) {
            if (space.isItWall()) {
                allWallsCount++;
            }
        }
    }

    public void healSurvivor(Survivor survivor) {
        if (isSafeZoneDestroyed()) return;
        if (RNG.nextFloat() < chanceToHeal) {
            survivor.changeHealthLevel(10);
        }
    }

    public void commitWallDestruction() {
        destroyedWallsCount++;
        if (isSafeZoneDestroyed()) {
            destroySafeZone();
        }
    }

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

    public boolean isSafeZoneDestroyed() {
        if (allWallsCount == 0) return false;
        return (float) destroyedWallsCount / allWallsCount >= destructionThreshold;
    }

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