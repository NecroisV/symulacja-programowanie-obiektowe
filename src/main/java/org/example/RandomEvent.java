package org.example;

public abstract class RandomEvent {
    private int actualDuration;
    private int maximumDuration;
    private int timeSinceLastSpawn;
    private int minimalTimeForNextSpawn;
    private float chanceToSpawn;

    protected RandomEvent(){

    }

    public void addEffect(SimulationEnvironment s){

    }

    public boolean checkEarlyEnd(){
        return true;
    }

    public boolean checkIfSpawns(){
        return true;
    }

    public void updateTime(){

    }
}
