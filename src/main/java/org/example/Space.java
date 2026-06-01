package org.example;

import java.util.ArrayList;
import java.util.List;

public class Space {
    private int x;
    private int y;
    private boolean isWall;
    private environmentalResource resource;
    private List<Agent> agents = new ArrayList<>();
    private List<Equipment> equipmentOnGround = new ArrayList<>();

    private Space up;
    private Space right;
    private Space down;
    private Space left;

    public Space(int given_x, int given_y){
        x = given_x;
        y = given_y;
    }

    public float calculateSurvivorWeight(Survivor o){
        return 0.0f;
    }

    public float calculateInfectedWeight(Infected z){
        return 0.0f;
    }

    public void createWall(){
        isWall =true;}

    public boolean isInSafeZone(){
        return false;
    }

    public boolean isItWall(){return isWall;}
    public void destroyWall(){
        this.isWall = false;
    }

    public void joinUp(Space given_up){
        up = given_up;
    }
    public void joinRight(Space given_right){
        right = given_right;
    }
    public void joinDown(Space given_down){
        down = given_down;
    }
    public void joinLeft(Space given_left){
        left = given_left;
    }

    public void addAgent(Agent agent){
        agents.add(agent);
    }
    public void deleteAgent(Agent agent){
        agents.remove(agent);
    }

    public List<Agent> getAgents(){return agents;}

    public int[] getPosition(){return new int[]{x, y};}

    public Space getUp(){return up;}
    public Space getRight(){return right;}
    public Space getDown(){return down;}
    public Space getLeft(){return left;}

    public void addEquipment(Equipment equipment) {
        equipmentOnGround.add(equipment);
    }

    public boolean hasEquipment() {
        return !equipmentOnGround.isEmpty();
    }

    public Equipment pickUpEquipment() {
        if (!equipmentOnGround.isEmpty()) {
            return equipmentOnGround.removeFirst();
        }
        return null;
    }

    public List<Equipment> getEquipmentOnGround() {
        return equipmentOnGround;
    }
}
