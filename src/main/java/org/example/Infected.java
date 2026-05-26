package org.example;

public class Infected extends Agent{
    private float infectionChance;
    private float chanceToHeal;
    private int dataTransmissionDistance;
    private InfectedMemory memory;

    public Infected(int given_x, int given_y, int given_health){
        super(given_x, given_y, given_health);
    }
    public void fightSurvivor(Survivor target){

    }

    public void shareMemory(Infected other){

    }

    public void healWound(){

    }

}
