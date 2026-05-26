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

    }

    public void changeHealthLevel(int ammount){

    }

    public int calcualteFOV(float lightLevel){
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
}
