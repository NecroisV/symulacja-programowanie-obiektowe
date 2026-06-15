package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Zakażony agent - poluje na ocalałych i unika innych zakażonych
public class Infected extends Agent {

    public Infected(int given_x, int given_y, int given_health, int given_strength, int given_FOV, int given_speed) {
        super(given_x, given_y, given_health, given_strength, given_FOV, given_speed);
    }

    // Oblicza wagi dla pól - zakażeni dążą do ocalałych, unikają siebie nawzajem i stref bezpieczeństwa
    @Override
    public void getAgentWeights(Space start, Map<String, Integer> baseWeights, int weightDivisor) {
        List<Space> localArea = getLocalArea(start);

        ArrayList<ArrayList<Space>> thingsAgentSaw = this.whatAgentSaw(start);
        List<Space> seenAgents = thingsAgentSaw.get(1);

        // Reset wag w lokalnym obszarze
        for (Space space : localArea) {
            space.changeWeight(-space.getWeight());
            space.changeWeight(1);
        }

        for (Space space : localArea) {
            if (seenAgents.contains(space)) {
                for (Agent a : space.getAgents()) {
                    if (a instanceof Survivor && !space.isInSafeZone()) {
                        // Wysoka waga dla pól z widzianymi ocalałymi
                        addWeightWithSpill(space, baseWeights.getOrDefault("infectedCurrentSeenSurvivor", 100), weightDivisor);
                    } else if (a instanceof Infected && a != this) {
                        // Niska waga dla pól z innymi zakażonymi (unikają się)
                        addWeightWithSpill(space, baseWeights.getOrDefault("infectedCloseInfected", 5), weightDivisor);
                        space.changeWeight(-256128 - space.getWeight());
                    }
                }
            }
            // Unikanie stref bezpieczeństwa
            if (space.isInSafeZone()) {
                space.changeWeight(-1000 * Math.abs(space.getWeight()));
            }
        }
    }
}