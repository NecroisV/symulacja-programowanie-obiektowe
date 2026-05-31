package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Infected extends Agent {
    private float infectionChance;
    private float chanceToHeal;
    private int dataTransmissionDistance;
    private InfectedMemory memory;

    public Infected(int given_x, int given_y, int given_health, Space[][] board) {
        super(given_x, given_y, given_health, board);
        // this.memory = new InfectedMemory();
    }


    public void fightSurvivor(Survivor target) {
        int damageToSurvivor = this.calculateStrength() - target.calculateStrength();
        target.changeHealthLevel(Math.min(-damageToSurvivor, 0));
    }
    public void shareMemory(Infected other) {}
    public void healWound() {}

    public void getAgentWeights(Space start, Map<String, Integer> baseWeights, int weightDivisor) {
        Space[][] board = getBoard();

        ArrayList<ArrayList<Space>> thingsAgentSaw = this.whatAgentSaw(start);
        List<Space> seenAgents = thingsAgentSaw.get(1);

        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                Space space = board[y][x];
                if (space != null) {
                    space.changeWeight(-space.getWeigth());
                    space.changeWeight(1);
                }
            }
        }

        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                Space space = board[y][x];
                if (space == null || space.isItWall()) continue;

                if (seenAgents.contains(space)) {
                    for (Agent a : space.getAgents()) {
                        if (a instanceof Survivor) {
                            addWeightWithSpill(space, baseWeights.getOrDefault("infectedCurrentSeenSurvivor", 100), weightDivisor);

                            /*
                            if (this.memory != null) {
                                this.memory.updateLastKnownPosition(space);
                            }
                            */
                        }
                        else if (a instanceof Infected && a != this) {
                            addWeightWithSpill(space, baseWeights.getOrDefault("infectedCloseInfected", 5), weightDivisor);
                            space.changeWeight(-space.getWeigth());
                        }
                    }
                }

                /*
                if (this.memory != null) {
                    if (this.memory.isLastKnownPosition(space)) {
                        addWeightWithSpill(space, baseWeights.getOrDefault("infectedMemory", 25), weightDivisor);
                    }
                }
                */
            }
        }

        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                Space space = board[y][x];
                if (space != null && space.isInSafeZone()) {
                    space.changeWeight(-space.getWeigth());
                }
            }
        }
    }

    protected void addWeightWithSpill(Space space, int weight, int divisor) {
        if (space == null || space.isItWall()) return;

        space.changeWeight(weight);

        if (divisor <= 1) return;
        int spilledWeight = weight / divisor;
        if (spilledWeight == 0) return;

        Space[] neighbours = {space.getUp(), space.getRight(), space.getDown(), space.getLeft()};
        for (Space n : neighbours) {
            if (n != null && !n.isItWall()) {
                n.changeWeight(spilledWeight);
            }
        }
    }

    public float getInfectionChance() {
        return this.infectionChance;
    }
}