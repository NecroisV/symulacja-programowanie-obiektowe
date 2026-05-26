package org.example;

import java.util.List;
import static java.lang.Math.round;

public class Survivor extends Agent{
    private int energyLevel = 100;
    private List<Space> visitedSpacesMemory;
    private int equipmentCapacity;
    private List<Equipment> equipment;
    private float chanceToHeal;
    private boolean isStarving = false;

    public Survivor(int given_x, int given_y, int given_health){
        super(given_x, given_y, given_health);
    }

    public void changeEnergyLevel(int amount){

    }

    public void useResource(environmentalResource resource){

    }

    public void pickUpEquipment(Equipment ekw){

    }

    public boolean hasFreeSlot(){
        return true;
    }

    public void starve(){

    }

    public void fightSurvivor(Survivor other){

    }

    public void fightInfected(Infected z){

    }

    public void steal(Survivor loser){

    }

    public void healWound(){

    }

    public Infected transformIntoInfected(Infected z){
        int[] position = getPosition();

        return new Infected(position[0], position[1], (int) round(z.getHealth() / 2.0));
    }
}
