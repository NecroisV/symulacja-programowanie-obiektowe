package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.lang.Math.round;

public class Survivor extends Agent {
    private int energyLevel = 100;
    private List<Space> visitedSpacesMemory;
    private int equipmentCapacity = 5;
    private List<Equipment> equipment = new ArrayList<>();
    private float chanceToHeal;
    private boolean isStarving = false;

    public Survivor(int given_x, int given_y, int given_health, Space[][] board) {
        super(given_x, given_y, given_health, board);
    }

    public void changeEnergyLevel(int amount) {
        energyLevel += amount;
        if (energyLevel < 0) energyLevel = 0;
    }

    public void useResource(environmentalResource resource) {}
    public void pickUpEquipment(Equipment ekw) {
        if (hasFreeEquipmentSlot()) {
            equipment.add(ekw);
        }
    }
    public boolean hasFreeSlot() { return true; }
    public void starve() { isStarving = true; }
    public void fightSurvivor(Survivor other) {}

    public void fightInfected(Infected z) {
        int damageToInfected = (this.calculateStrength()) - z.calculateStrength();
        z.changeHealthLevel(Math.min(-damageToInfected, -5));
    }
    public void steal(Survivor loser) {
        int stolenEnergy = loser.getEnergyLevel()-10;
        this.changeEnergyLevel(Math.max(stolenEnergy,10));
        loser.changeEnergyLevel(-Math.max(stolenEnergy,10));

        List<Equipment> loserItems = new ArrayList<>(loser.equipment);

        for (Equipment item : loserItems) {
            if (this.hasFreeEquipmentSlot()) {
                this.equipment.add(item);
            }
        }

        loser.equipment.clear();
    }

    public List<Equipment> getEquipment() {
        return this.equipment;
    }

    public void healWound() {}

    public Infected transformIntoInfected(Infected z) {
        int[] position = getPosition();
        return new Infected(position[0], position[1], (int) round(z.getHealth() / 2.0), z.getBoard());
    }

    public void getAgentWeights(Space start, Map<String, Integer> baseWeights, int weightDivisor) {
        this.energyLevel-=5;
        Space[][] board = getBoard();
        int currentEnergy = getEnergyLevel();
        int strength = calculateStrength();

        ArrayList<ArrayList<Space>> thingsAgentSaw = this.whatAgentSaw(start);
        List<Space> seenResources = thingsAgentSaw.get(0);
        List<Space> seenAgents = thingsAgentSaw.get(1);

        for (Space[] value : board) {
            for (Space space : value) {
                if (space != null) {
                    space.changeWeight(-space.getWeight());
                    space.changeWeight(1);
                }
            }
        }

        for (Space[] spaces : board) {
            for (Space space : spaces) {
                if (space == null || space.isItWall()) continue;

                if (space.isInSafeZone()) {
                    addWeightWithSpill(space, baseWeights.getOrDefault("survivorSafeZone", 80), weightDivisor);
                }

                if (seenResources.contains(space)) {
                    addWeightWithSpill(space, baseWeights.getOrDefault("survivorResource", 30), weightDivisor);
                }

                if (seenAgents.contains(space)) {
                    for (Agent a : space.getAgents()) {
                        if (a instanceof Infected) {
                            int baseFear = baseWeights.getOrDefault("survivorInfected", -100);
                            int fearReduction = strength * 10;
                            int finalInfectedWeight = Math.min(0, baseFear + fearReduction);

                            addWeightWithSpill(space, finalInfectedWeight, weightDivisor);
                        } else if (a instanceof Survivor && a != this) {
                            int baseSurvivorWeight = baseWeights.getOrDefault("survivorSurvivor", -20);
                            if (currentEnergy < 40) {
                                baseSurvivorWeight += (currentEnergy) * 2;
                            }
                            addWeightWithSpill(space, baseSurvivorWeight, weightDivisor);
                        }
                    }
                }
            }
        }
    }

    protected void addWeightWithSpill(Space space, int weight, int divisor) {
        if (space == null || space.isItWall()) return;

        space.changeWeight(weight);

        if (divisor <= 1) return;
        int spilledWeight = weight / divisor;
        if (spilledWeight == 0) return;

        Space[] neighbours = {space.getUp(), space.getRight(), space.getDown(), space.getLeft()};
        for (Space n : neighbours) {
            if (n != null && !n.isItWall()) {
                n.changeWeight(spilledWeight);
            }
        }
    }

    public int getEnergyLevel() { return energyLevel; }
    public boolean hasFreeEquipmentSlot() { return (equipmentCapacity - equipment.size() > 0); }

}