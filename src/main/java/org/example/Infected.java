package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Infected extends Agent {
    private float infectionChance;
    private float chanceToHeal;
    private int dataTransmissionDistance;
    private InfectedMemory memory;

    public Infected(int given_x, int given_y, int given_health, int given_strength, int given_FOV, int given_speed) {
        super(given_x, given_y, given_health, given_strength, given_FOV, given_speed);
    }

    public void fightSurvivor(Survivor target) {
        int damageToSurvivor = this.calculateStrength() - target.calculateStrength();
        target.changeHealthLevel(Math.min(-damageToSurvivor, 0));
    }

    public void shareMemory(Infected other) {}

    @Override
    public void getAgentWeights(Space start, Map<String, Integer> baseWeights, int weightDivisor) {
        List<Space> localArea = getLocalArea(start);

        ArrayList<ArrayList<Space>> thingsAgentSaw = this.whatAgentSaw(start);
        List<Space> seenAgents = thingsAgentSaw.get(1);

        for (Space space : localArea) {
            space.changeWeight(-space.getWeight());
            space.changeWeight(1);
        }

        for (Space space : localArea) {
            if (seenAgents.contains(space)) {
                for (Agent a : space.getAgents()) {
                    if (a instanceof Survivor) {
                        addWeightWithSpill(space, baseWeights.getOrDefault("infectedCurrentSeenSurvivor", 100), weightDivisor);
                    } else if (a instanceof Infected && a != this) {
                        addWeightWithSpill(space, baseWeights.getOrDefault("infectedCloseInfected", 5), weightDivisor);
                        space.changeWeight(-20 * space.getWeight());
                    }
                }
            }
        }

        for (Space space : localArea) {
            if (space.isInSafeZone()) {
                space.changeWeight(-space.getWeight());
            }
        }
    }

    public float getInfectionChance() {
        return this.infectionChance;
    }
}