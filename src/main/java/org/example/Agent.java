package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Agent {
    private int x;
    private int y;
    private int age = 0;
    private int healthLevel;
    private int baseStrength;
    private int baseFOV = 5;
    private int baseSpeed = 2;
    private boolean isAlive = true;
    private List<Wound> wounds;
    private int weaponCapacity = 1;
    private int clothesCapacity = 1;
    protected Space[][] copiedBoard;

    protected Agent(int given_x, int given_y, int given_health, Space[][] board){
        x = given_x;
        y = given_y;
        healthLevel = given_health;
        copiedBoard = board;
    }

    public abstract void getAgentWeights(Space start, Map<String, Integer> weights, int divisor);

    public int[] makeMove(Space start, Map<String, Integer> weights, int divisor) {
        getAgentWeights(start, weights, divisor);
        ArrayList<Space> availableSpaces = possibleMove(start);

        if (availableSpaces.isEmpty()) return start.getPosition();

        int totalWeightSum = 0;
        List<Integer> validWeights = new ArrayList<>();

        for (Space s : availableSpaces) {
            int w = Math.max(0, s.getWeight());
            validWeights.add(w);
            totalWeightSum += w;
        }

        if (totalWeightSum == 0) {
            Space bestWorst = availableSpaces.getFirst();
            for(Space s : availableSpaces){
                if(s.getWeight() > bestWorst.getWeight()) {
                    bestWorst = s;
                }
            }
            return bestWorst.getPosition();
        }

        Random random = new Random();
        int rolledValue = random.nextInt(totalWeightSum);
        int currentSum = 0;
        for (int i = 0; i < availableSpaces.size(); i++) {
            currentSum += validWeights.get(i);
            if (rolledValue < currentSum) {
                return availableSpaces.get(i).getPosition();
            }
        }
        return availableSpaces.getLast().getPosition();
    }

    protected void addWeightWithSpill(Space s, int weight, int divisor) {
        if (s == null || s.isItWall()) return;
        s.changeWeight(weight);
        if (divisor <= 1) return;
        int spilled = weight / divisor;
        Space[] n = {s.getUp(), s.getRight(), s.getDown(), s.getLeft()};
        for (Space neighbor : n){
            if (neighbor != null && !neighbor.isItWall()){
                neighbor.changeWeight(spilled);
            }
        }
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

    public void die() {
        this.isAlive = false;
        this.healthLevel = 0;
    }

    public void changeHealthLevel(int amount) {
        this.healthLevel += amount;
        if (this.healthLevel <= 0) {
            die();
        }
    }

    public ArrayList<ArrayList<Space>> whatAgentSaw(Space start){
        int actualFOV = calculateFOV(3);
        ArrayList<Space> seenSpaces = new ArrayList<>();

        ArrayList<Space> que = new ArrayList<>();
        ArrayList<Integer> distances = new ArrayList<>();

        ArrayList<Space> seenResourceList = new ArrayList<>();
        ArrayList<Space> seenAgentList = new ArrayList<>();

        que.add(start);
        distances.add(0);
        seenSpaces.add(start);

        int analysedIndex = 0;

        while (analysedIndex < que.size()) {
            Space actual = que.get(analysedIndex);
            int actualDistance = distances.get(analysedIndex);
            analysedIndex++;

            if (actualDistance >= actualFOV) {
                continue;
            }

            Space[] neighbours = {actual.getUp(), actual.getRight(), actual.getDown(), actual.getLeft()};

            for (Space n : neighbours) {
                if (n != null && !seenSpaces.contains(n)) {
                    que.add(n);
                    distances.add(actualDistance + 1);
                    seenSpaces.add(n);
                    if(n.containsResource()){seenResourceList.add(n);}
                    if(n.containsAgents()){seenAgentList.add(n);}
                }
            }
        }

        ArrayList<Space> trulySeenResourceList = new ArrayList<>();
        ArrayList<Space> trulySeenAgentList = new ArrayList<>();

        for (Space resourceSpace : seenResourceList) {
            if (hasLineOfSight(start, resourceSpace)) {
                trulySeenResourceList.add(resourceSpace);
            }
        }

        for (Space agentSpace : seenAgentList) {
            if (hasLineOfSight(start, agentSpace)) {
                trulySeenAgentList.add(agentSpace);
            }
        }

        ArrayList<ArrayList<Space>> seenThing = new ArrayList<>();
        seenThing.add(trulySeenResourceList);
        seenThing.add(trulySeenAgentList);

        return seenThing;
    }

    private boolean hasLineOfSight(Space start, Space target) {
        int[] pos0 = start.getPosition();
        int[] pos1 = target.getPosition();

        int x0 = pos0[0];
        int y0 = pos0[1];
        int x1 = pos1[0];
        int y1 = pos1[1];

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if ((x0 != pos0[0] || y0 != pos0[1]) && copiedBoard[y0][x0].isItWall()) {
                return false;
            }

            if (x0 == x1 && y0 == y1) {
                break;
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
        return true;
    }

    private int calculateFOV(float lightLevel){
        return baseFOV;
    }

    public int calculateStrength(){
        if(this instanceof Survivor){
            return 10;
        }
        else{
            return 5;
        }
    }

    public int calculateSpeed(){
        return 10;
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

    public Space[][] getBoard(){
        return copiedBoard;
    }

    public List<Equipment> getEquipment() {
        return equipment;
    }

}

    public void getAgentWeights(Space start) {}
}