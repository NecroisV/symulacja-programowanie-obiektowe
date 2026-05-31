package org.example;

import java.util.ArrayList;
import java.util.List;

public class Space {
    private int x;
    private int y;
    private boolean isWall;
    private environmentalResource resource;
    private List<Agent> agents = new ArrayList<>();

    private Space up;
    private Space right;
    private Space down;
    private Space left;

    private int weigth = 1;

    public Space(int given_x, int given_y){
        x = given_x;
        y = given_y;
    }

    public void createWall(){
        isWall = true;}

    public void destroyWall(){
    }

    public boolean isInSafeZone(){
        return false;
    }

    public boolean isItWall(){return isWall;}

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

    public boolean containsResource(){
        return resource != null && !resource.wasUsed();
    }

    public boolean containsAgents(){
        return !agents.isEmpty();
    }

    public void changeWeight(int change){
        weigth += change;
    }

    public int getWeigth(){
        return weigth;
    }

}
