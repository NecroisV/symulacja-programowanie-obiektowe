package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Infected extends Agent {

    public Infected(int given_x, int given_y, int given_health, int given_strength, int given_FOV, int given_speed) {
        super(given_x, given_y, given_health, given_strength, given_FOV, given_speed);
    }



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
                    if (a instanceof Survivor && !space.isInSafeZone()) {
                        addWeightWithSpill(space, baseWeights.getOrDefault("infectedCurrentSeenSurvivor", 100), weightDivisor);
                    } else if (a instanceof Infected && a != this) {
                        addWeightWithSpill(space, baseWeights.getOrDefault("infectedCloseInfected", 5), weightDivisor);
                        space.changeWeight(-256128 - space.getWeight());
                    }
                }
            }
            if (space.isInSafeZone()) {
                space.changeWeight(-1000 * Math.abs(space.getWeight()));
            }
        }
    }
}