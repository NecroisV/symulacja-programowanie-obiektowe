package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Agent {
    private int x;
    private int y;
    private int age = 0;
    private int healthLevel;
    private int baseStrength;
    private int baseFOV;
    private int baseSpeed = 1;
    private boolean isAlive = true;
    private List<Wound> wounds;
    private int weaponCapacity = 1;
    private int clothesCapacity = 1;

    protected Agent(int given_x, int given_y, int given_health){
        x = given_x;
        y = given_y;
        healthLevel = given_health;
    }

    public int[] makeMove(Space start){
        ArrayList<Space> availableSpaces = possibleMove(start);

        Random random = new Random();
        Space targetSpace = availableSpaces.get(random.nextInt(availableSpaces.size()));
        return targetSpace.getPosition();
    }

    private ArrayList<Space> possibleMove(Space start) {
        ArrayList<Space> visitedSpaces = new ArrayList<>();

        if (start.isItWall()) {
            return visitedSpaces;
        }

        ArrayList<Space> que = new ArrayList<>();
        ArrayList<Integer> distances = new ArrayList<>();

        que.add(start);
        distances.add(0);
        visitedSpaces.add(start);

        int analysedIndex = 0;

        while (analysedIndex < que.size()) {
            Space actual = que.get(analysedIndex);
            int actualDistance = distances.get(analysedIndex);
            analysedIndex++;

            if (actualDistance >= baseSpeed) {
                continue;
            }

            Space[] neighbours = {actual.getUp(), actual.getRight(), actual.getDown(), actual.getLeft()};

            for (Space n : neighbours) {
                if (n != null && !n.isItWall() && !visitedSpaces.contains(n)) {
                    que.add(n);
                    distances.add(actualDistance + 1);
                    visitedSpaces.add(n);
                }
            }
        }

        return visitedSpaces;
    }

    public void ageUp(){
        age++;
    }

    public void die(){
        this.isAlive = false;
    }

    public void changeHealthLevel(int amount){

    }

    public int calculateFOV(float lightLevel){
        return 0;
    }

    public int calculateStrength(){
        return 0;
    }

    public int calculateSpeed(){
        return 0;
    }

    public boolean isItAlive(){return isAlive;}

    public int[] getPosition(){
        return new int[]{x, y};
    }

    public int getHealth(){return healthLevel;}

    public void reviveWound(){
    }
    private List<Equipment> equipment = new ArrayList<>();

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

    public List<Equipment> getEquipment() {
        return equipment;
    }

}
