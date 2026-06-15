package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.round;

// Ocalały agent - zbiera zasoby, unika zakażonych, walczy, może się przemienić
public class Survivor extends Agent {
    private int energyLevel = 100;
    private int maxEnergyLevel = energyLevel;
    private int weaponCapacity = 2;
    private int clothesCapacity = 2;
    private List<Equipment> equipment = new ArrayList<>();
    private boolean isStarving = false;

    public Survivor(int given_x, int given_y, int given_health, int given_strength, int given_FOV, int given_speed) {
        super(given_x, given_y, given_health, given_strength, given_FOV, given_speed);
    }

    // Zmienia poziom energii (z ograniczeniem do max)
    public void changeEnergyLevel(int amount) {
        energyLevel += amount;
        if (energyLevel > maxEnergyLevel) energyLevel = maxEnergyLevel;
        if (energyLevel < 0){
            energyLevel = 0;
            isStarving = true;
            starve();
        }
    }

    // Głodowanie - zadaje obrażenia co tick
    public void starve() {
        if(isStarving){
            this.changeHealthLevel(-2);
        }
    }

    public boolean isStarving(){
        return isStarving;
    }

    // Okrada innego ocalałego (energia i ekwipunek)
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

    // Przemienia ocalałego w zakażonego (połowa zdrowia zakażonego)
    public Infected transformIntoInfected(Infected i) {
        int[] position = getPosition();
        return new Infected(position[0], position[1], (int) round(i.getHealth() / 2.0), i.calculateStrength(), i.calculateFOV(), i.calculateSpeed());
    }

    // Zwraca ekwipunek danego typu (broń lub ubrania)
    public <T extends Equipment> List<T> getEquipmentOfType(Class<T> type) {
        return equipment.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(java.util.stream.Collectors.toList());
    }

    // Podnosi ekwipunek z pola (sprawdza limity miejsca)
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

    // Dodaje ekwipunek bezpośrednio (przy starcie)
    public void getEquipment(Equipment item){
        if(item instanceof Weapon && getEquipmentOfType(Weapon.class).size() >= weaponCapacity){
            equipment.add(item);
        }
        else if (item instanceof Clothes && getEquipmentOfType(Clothes.class).size() >= clothesCapacity) {
            equipment.add(item);
        }
    }

    // Sprawdza czy jest miejsce w ekwipunku na dany przedmiot
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

    // Oblicza wagi dla pól - ocaleni unikają zakażonych, szukają zasobów, ekwipunku i stref bezpieczeństwa
    @Override
    public void getAgentWeights(Space start, Map<String, Integer> baseWeights, int weightDivisor) {
        int currentEnergy = getEnergyLevel();
        int strength = calculateStrength();

        List<Space> localArea = getLocalArea(start);

        ArrayList<ArrayList<Space>> thingsAgentSaw = this.whatAgentSaw(start);
        List<Space> seenResources = thingsAgentSaw.get(0);
        List<Space> seenAgents = thingsAgentSaw.get(1);
        List<Space> seenEquipment = thingsAgentSaw.get(2);

        // Reset wag w lokalnym obszarze
        for (Space space : localArea) {
            space.changeWeight(-space.getWeight());
            space.changeWeight(1);
        }

        for (Space space : localArea) {
            // Preferowanie stref bezpieczeństwa (jeśli nie jest się w jednej)
            if (space.isInSafeZone() && !start.isInSafeZone()) {
                addWeightWithSpill(space, baseWeights.getOrDefault("survivorSafeZone", 80), weightDivisor);
            }

            // Preferowanie zasobów
            if (seenResources.contains(space)) {
                addWeightWithSpill(space, baseWeights.getOrDefault("survivorResource", 30), weightDivisor);
            }

            // Preferowanie ekwipunku (tylko jeśli jest miejsce)
            if (seenEquipment.contains(space)) {
                for (Equipment equipment : space.getEquipmentOnGround()) {
                    if (this.hasSpaceInInventory(equipment)) {
                        addWeightWithSpill(space, baseWeights.getOrDefault("survivorEquipment", 50), weightDivisor);
                    }
                }
            }

            // Reakcja na widzianych agentów
            if (seenAgents.contains(space)) {
                for (Agent a : space.getAgents()) {
                    if (a instanceof Infected) {
                        // Unikanie zakażonych (strach zmniejszany przez siłę)
                        int baseFear = baseWeights.getOrDefault("survivorInfected", -100);
                        int fearReduction = strength * 10;
                        int finalInfectedWeight = Math.min(0, baseFear + fearReduction);
                        addWeightWithSpill(space, finalInfectedWeight, weightDivisor);
                    } else if (a instanceof Survivor && a != this) {
                        // Unikanie innych ocalałych (szczególnie gdy niska energia)
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