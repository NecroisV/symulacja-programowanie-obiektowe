package org.example;

public class environmentalResource {
    private int energyRecovery;
    private int healthRecovery;
    private int minimalRespawnTime;
    private int timeFromUse;
    private boolean isUsed;

    public environmentalResource(){
        energyRecovery = RNG.nextInt(10, 15);
        healthRecovery = RNG.nextInt(5, 10);
        minimalRespawnTime = RNG.nextInt(5, 7);
        timeFromUse = -1;
        isUsed = false;
    }

    public int[] getUsed(){
        isUsed = true;
        timeFromUse = 0;
        return new int[] {energyRecovery, healthRecovery};
    }

    public boolean wasUsed(){
        return isUsed;
    }

    public void updateTime(){
        timeFromUse += 1;
    }

    public void Respawn(){
        if(RNG.nextInt(minimalRespawnTime)+timeFromUse>minimalRespawnTime){
            isUsed = false;
        }
    }
}
