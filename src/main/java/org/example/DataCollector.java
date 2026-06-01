package org.example;

public class DataCollector {
    private int survivorAmount;
    private int infectedAmount;
    private int survivorInfectedInteractions = 0;
    private int survivorSurvivorInteractions = 0;
    private int healedWoundInSafeZones = 0;
    private float meanHealth;
    private int timeToSurvivorsExtinction = -1;

    public DataCollector() {
    }

    public void updateData(SimulationEnvironment s) {
        int survivors = 0;
        int infected = 0;
        double totalHealth = 0;
        int totalAgents = 0;

        for (Agent a : s.getAgentList()) {
            if (a.isItAlive()) {
                if (a instanceof Survivor) {
                    survivors++;
                } else if (a instanceof Infected) {
                    infected++;
                }
                totalHealth += a.getHealth();
                totalAgents++;
            }
        }

        this.survivorAmount = survivors;
        this.infectedAmount = infected;
        this.meanHealth = totalAgents > 0 ? (float) (totalHealth / totalAgents) : 0f;

        if (survivors == 0 && this.timeToSurvivorsExtinction == -1 && s.getActualTick() > 0) {
            this.timeToSurvivorsExtinction = s.getActualTick();
        }
    }

    public void incSurvivorInfectedInteractions() { this.survivorInfectedInteractions++; }
    public void incSurvivorSurvivorInteractions() { this.survivorSurvivorInteractions++; }
    public void incHealedWoundInSafeZones() { this.healedWoundInSafeZones++; }

    public int getSurvivorAmount() { return survivorAmount; }
    public int getInfectedAmount() { return infectedAmount; }
    public int getSurvivorInfectedInteractions() { return survivorInfectedInteractions; }
    public int getSurvivorSurvivorInteractions() { return survivorSurvivorInteractions; }
    public int getHealedWoundInSafeZones() { return healedWoundInSafeZones; }
    public float getMeanHealth() { return meanHealth; }
    public int getTimeToSurvivorsExtinction() { return timeToSurvivorsExtinction; }
}