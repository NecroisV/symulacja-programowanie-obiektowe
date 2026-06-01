package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.lang.Math.round;

public class Survivor extends Agent {
    private int energyLevel = 100;
    private List<Space> visitedSpacesMemory;
    private int equipmentCapacity = 5;
    private int weaponCapacity = 2;
    private int clothesCapacity = 2;
    private List<Equipment> equipment = new ArrayList<>();
    private float chanceToHeal;
    private boolean isStarving = false;

    public Survivor(int given_x, int given_y, int given_health) {
        super(given_x, given_y, given_health);
    }

    public void changeEnergyLevel(int amount) {
        energyLevel += amount;
        if (energyLevel < 0) energyLevel = 0;
    }

    public void useResource(environmentalResource resource) {
    }

    public void pickUpEquipment(Equipment ekw) {
        if (hasSpaceInInventory(ekw)) {
            equipment.add(ekw);
        }
    }

    public boolean hasFreeSlot() {
        return true;
    }

    public void starve() {
        isStarving = true;
    }

    public void fightSurvivor(Survivor other) {
    }

    public void fightInfected(Infected z) {
        int damageToInfected = (this.calculateStrength()) - z.calculateStrength();
        z.changeHealthLevel(Math.min(-damageToInfected, -5));
    }

    public void steal(Survivor loser) {
        int stolenEnergy = loser.getEnergyLevel() - 10;
        this.changeEnergyLevel(Math.max(stolenEnergy, 10));
        loser.changeEnergyLevel(-Math.max(stolenEnergy, 10));

        List<Equipment> loserItems = new ArrayList<>(loser.equipment);

        for (Equipment item : loserItems) {
            if (this.hasSpaceInInventory(item)) {
                this.equipment.add(item);
            }
        }

        loser.equipment.clear();
    }

    public List<Equipment> getEquipment() {
        return this.equipment;
    }

    public void healWound() {
    }

    public Infected transformIntoInfected(Infected z) {
        int[] position = getPosition();
        return new Infected(position[0], position[1], (int) round(z.getHealth() / 2.0));
    }

    public <T extends Equipment> List<T> getEquipmentOfType(Class<T> type) {
        return equipment.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean pickUpEquipment(Space space) {
        Equipment item = space.pickUpEquipment();
        if (item == null) return false;

        if (item instanceof Weapon && getEquipmentOfType(Weapon.class).size() >= weaponCapacity) {
            space.addEquipment(item);
            return false;
        }
        if (item instanceof Clothes && getEquipmentOfType(Clothes.class).size() >= clothesCapacity) {
            space.addEquipment(item);
            return false;
        }

        equipment.add(item);
        return true;
    }

    public boolean hasSpaceInInventory(Equipment item) {
        if (item instanceof Weapon) {
            return getEquipmentOfType(Weapon.class).size() < weaponCapacity;
        }
        if (item instanceof Clothes) {
            return getEquipmentOfType(Clothes.class).size() < clothesCapacity;
        }
        return false;
    }

    public int getEnergyLevel() {
        return energyLevel;
    }

    @Override
    public void getAgentWeights(Space start, Map<String, Integer> baseWeights, int weightDivisor) {
        this.energyLevel -= 5;
        int currentEnergy = getEnergyLevel();
        int strength = calculateStrength();

        List<Space> localArea = getLocalArea(start);

        ArrayList<ArrayList<Space>> thingsAgentSaw = this.whatAgentSaw(start);
        List<Space> seenResources = thingsAgentSaw.get(0);
        List<Space> seenAgents = thingsAgentSaw.get(1);
        List<Space> seenEquipment = thingsAgentSaw.get(2);

        for (Space space : localArea) {
            space.changeWeight(-space.getWeight());
            space.changeWeight(1);
        }

        for (Space space : localArea) {
            if (space.isInSafeZone()) {
                addWeightWithSpill(space, baseWeights.getOrDefault("survivorSafeZone", 80), weightDivisor);
            }

            if (seenResources.contains(space)) {
                addWeightWithSpill(space, baseWeights.getOrDefault("survivorResource", 30), weightDivisor);
            }

            if (seenEquipment.contains(space)) {
                for (Equipment equipment : space.getEquipmentOnGround()) {
                    if (this.hasSpaceInInventory(equipment)) {
                        addWeightWithSpill(space, baseWeights.getOrDefault("survivorEquipment", 50), weightDivisor);
                    }
                }
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