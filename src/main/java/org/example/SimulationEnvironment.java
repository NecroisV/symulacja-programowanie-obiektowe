package org.example;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class SimulationEnvironment {
    private static SimulationEnvironment instance;
    private Space[][] board;
    private List<Agent> agentList = new ArrayList<>();
    private Set<Agent> usedAgentList = new HashSet<>();
    private int actualTick = 0;
    private float lightLevel;
    private TimeOfDay timeOfDay;
    private SimulationParameters parameters = SimulationParameters.getInstance();
    private DataCollector data = new DataCollector();
    private List<SafeZone> zones = new ArrayList<>();
    private final List<String> turnLogs = new ArrayList<>();

    private EquipmentSpawnStrategy equipmentSpawnStrategy;

    public SimulationEnvironment(int width, int height){
        this.equipmentSpawnStrategy = this::spawnEquipmentRandomly;
        createBoard(width, height);
        int[] agentNumbers = parameters.getAgentsAmount();
        int[] chances = parameters.getEqAndWoundChances();
        createAgents(agentNumbers[0], agentNumbers[1], chances[0], chances[1]);
        spawnEquipmentOnBoard();

        data.updateData(this);
    }

    public void simulationStep(GraphicsContext gc, double tileSize){
        considerRandomEvent();
        moveAgents();
        considerInteractions();
        deleteDeadAgents();
        updateTimeOfDay();

        data.updateData(this);
        Render render = new Render();
        render.renderBoard(gc, tileSize, turnLogs, data, actualTick, board);
    }

    public List<Agent> getAgentList() { return agentList; }
    public int getActualTick() { return this.actualTick; }

    private void updateTimeOfDay(){

        actualTick++;
    }
//
//    private void calculateLightLevel(){
//        lightLevel = 1.0f;
//    }

    private void createBoard(int width, int height){
        board = new Space[height][width];
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                board[y][x] = new Space(x, y);
                if(x>0){
                    board[y][x].joinLeft(board[y][x-1]);
                    board[y][x-1].joinRight(board[y][x]);
                }
                if(x== width -1){
                    board[y][0].joinLeft(board[y][x]);
                    board[y][x].joinRight(board[y][0]);
                }
                if(RNG.nextInt(100)>=90) {
                    board[y][x].createWall();}
            }

            if(y>0){
                for(int x = 0; x < width; x++){
                    board[y][x].joinUp(board[y-1][x]);
                    board[y-1][x].joinDown(board[y][x]);
                }
            }
            if(y== height -1){
                for(int x = 0; x < width; x++){
                    board[0][x].joinUp(board[y][x]);
                    board[y][x].joinDown(board[0][x]);
                }
            }
        }
    }

    private void createAgents(int survivorNumber, int infectedNumber, int equipmentChance, int woundChance){
        int width = board[0].length;
        int height = board.length;

        int health = 100;

        int i = 0;
        while(i < survivorNumber){
            int x = RNG.nextInt(width -1);
            int y = RNG.nextInt(height -1);

            if (!board[y][x].isItWall()){
                Survivor survivor = new Survivor(x, y, health);
                agentList.add(survivor);
                board[y][x].addAgent(survivor);
                i ++;
            }
        }

        i = 0;
        while(i < infectedNumber){
            int x = RNG.nextInt(width -1);
            int y = RNG.nextInt(height -1);

            if (!board[y][x].isItWall()){
                Infected infected = new Infected(x, y, health);
                agentList.add(infected);
                board[y][x].addAgent(infected);
                i ++;
            }
        }
    }

    private void moveAgents(){
        int width = board[0].length;
        int height = board.length;
        usedAgentList.clear();

        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                if(!board[i][j].isItWall()){
                    List<Agent> originalAgents = board[i][j].getAgents();
                    if (!originalAgents.isEmpty()) {
                        List<Agent> copyAgents = new ArrayList<>(originalAgents);

                        for(Agent a : copyAgents){
                            if(!usedAgentList.contains(a)) {
                                usedAgentList.add(a);
                                int[] agentMove = a.makeMove(board[i][j], parameters.getMoveWeights(), 2);
                                displaceAgent(new int[]{i, j}, agentMove, a);
                            }
                        }
                    }
                }
            }
        }
    }

    private void displaceAgent(int[] originalSpace, int[] targetSpace, Agent a){
        int originalY = originalSpace[0];
        int originalX = originalSpace[1];

        int targetX = targetSpace[0];
        int targetY = targetSpace[1];
        board[targetY][targetX].addAgent(a);

        board[originalY][originalX].deleteAgent(a);
    }

    private void considerInteractions() {
        int width = board[0].length;
        int height = board.length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Space space = board[y][x];

                if (space != null && !space.isItWall()) {
                    if (space.getAgents().size() > 1) {
                        List<Agent> agentsOnSpace = new ArrayList<>(space.getAgents());
                        List<Survivor> survivors = new ArrayList<>();
                        List<Infected> infected = new ArrayList<>();

                        for (Agent a : agentsOnSpace) {
                            if (a.isItAlive()) {
                                if (a instanceof Survivor) survivors.add((Survivor) a);
                                else if (a instanceof Infected) infected.add((Infected) a);
                            }
                        }

                        if (infected.isEmpty() && survivors.size() > 1) {
                            data.incSurvivorSurvivorInteractions();

                            while (survivors.size() > 1) {
                                Survivor s1 = survivors.get(0);
                                Survivor s2 = survivors.get(1);

                                int s1Weight = Math.max(1, s1.calculateStrength());
                                int s2Weight = Math.max(1, s2.calculateStrength());
                                int totalWeight = s1Weight + s2Weight;

                                int roll = RNG.nextInt(totalWeight);

                                if (roll < s1Weight) {
                                    int damage = Math.max(5, s1Weight - (s2Weight / 2));
                                    s2.changeHealthLevel(-damage);
                                } else {
                                    int damage = Math.max(5, s2Weight - (s1Weight / 2));
                                    s1.changeHealthLevel(-damage);
                                }

                                if (!s2.isItAlive() || s2.getHealth() <= 0) {
                                    turnLogs.add("Ocalały pokonał innego ocalałego na pozycji [" + x + ", " + y + "]");
                                    s1.steal(s2);
                                    s2.die();
                                    space.deleteAgent(s2);
                                    survivors.remove(s2);
                                } else if (!s1.isItAlive() || s1.getHealth() <= 0) {
                                    turnLogs.add("⚔calały pokonał innego ocalałego na pozycji [" + x + ", " + y + "]");
                                    s2.steal(s1);
                                    s1.die();
                                    space.deleteAgent(s1);
                                    survivors.remove(s1);
                                }
                            }
                        }


                        if (!survivors.isEmpty() && !infected.isEmpty()) {
                            data.incSurvivorInfectedInteractions();

                            while (!survivors.isEmpty() && !infected.isEmpty()) {
                                Survivor survivor = survivors.getFirst();
                                Infected zakazony = infected.getFirst();

                                int zakazonyWeight = Math.max(1, zakazony.calculateStrength());
                                int survivorWeight = Math.max(1, survivor.calculateStrength());
                                int totalWeight = zakazonyWeight + survivorWeight;

                                int roll = RNG.nextInt(totalWeight);

                                if (roll < zakazonyWeight) {
                                    int damage = Math.max(5, zakazonyWeight - (survivorWeight / 2));
                                    survivor.changeHealthLevel(-damage);
                                    if (RNG.nextFloat() < zakazony.getInfectionChance()) {
                                        transformSurvivor(survivor, zakazony, x, y);
                                    }
                                } else {
                                    int damage = Math.max(5, survivorWeight - (zakazonyWeight / 2));
                                    zakazony.changeHealthLevel(-damage);
                                }

                                if (!zakazony.isItAlive() || zakazony.getHealth() <= 0) {
                                    turnLogs.add("Zakażony na pozycji [" + x + ", " + y + "] został permanentnie zlikwidowany!");
                                    zakazony.die();
                                    space.deleteAgent(zakazony);
                                    infected.remove(zakazony);
                                }

                                if (!survivor.isItAlive() || survivor.getHealth() <= 0) {
                                    turnLogs.add("Ocalały na pozycji [" + x + ", " + y + "] poległ w walce i zmarł.");
                                    survivor.die();
                                    space.deleteAgent(survivor);
                                    survivors.remove(survivor);
                                }
                            }
                        }
                    }

                    List<Agent> agentsOnSpace = new ArrayList<>(space.getAgents());
                    List<Survivor> survivors = new ArrayList<>();

                    for (Agent a : agentsOnSpace) {
                        if (a.isItAlive()) {
                            if (a instanceof Survivor) survivors.add((Survivor) a);
                        }
                    }

                    if(!survivors.isEmpty() && space.hasEquipment()) {
                        survivors.getFirst().pickUpEquipment(space);
                        int[] position = space.getPosition();
                        turnLogs.add("Ocalały na pozycji [" + position[0] + ", " + position[1] + "] podniósł ekwipunek");
                    }
                }
            }
        }
    }

    private void transformSurvivor(Survivor o, Infected z, int x, int y) {
        Infected newInfected = o.transformIntoInfected(z);
        turnLogs.add("Ocalały na pozycji [" + x + ", " + y + "] poległ i zmienił się w Zakażonego!");

        o.die();
        board[y][x].deleteAgent(o);
        agentList.add(newInfected);
        board[y][x].addAgent(newInfected);
    }

    private void deleteDeadAgents() {
        agentList.removeIf(a -> !a.isItAlive());
    }

    private void considerRandomEvent(){
        EventManager.runEventCheck(this.board);
        // data.incHealedWoundInSafeZones();
    }

    private void spawnEquipmentOnBoard() {
        int weaponCount = parameters.getWeaponCount();
        int clothesCount = parameters.getClothesCount();
        equipmentSpawnStrategy.spawnEquipment(board, weaponCount, clothesCount);
    }
    private void spawnItems(List<Space> freeSpaces, int count,
                            EquipmentFactory.EquipmentType type){
        int spawned = 0;
        int attempts = 0;
        while (spawned < count && attempts < freeSpaces.size()) {
            Space space = freeSpaces.get(RNG.nextInt(freeSpaces.size()));
            if (!space.hasEquipment()) {
                space.addEquipment(EquipmentFactory.createRandom(type));
                spawned++;
            }
            attempts++;
        }
    }
    private void spawnEquipmentRandomly(Space[][] board, int weaponCount, int clothesCount) {
        List<Space> freeSpaces = new ArrayList<>();
        for (Space[] row : board) {
            for (Space space : row) {
                if (!space.isItWall()) {
                    freeSpaces.add(space);
                }
            }
        }
        spawnItems(freeSpaces, weaponCount, EquipmentFactory.EquipmentType.WEAPON);
        spawnItems(freeSpaces, clothesCount, EquipmentFactory.EquipmentType.CLOTHES);
    }

    public Space[][] getBoard() {
        return board;
    }
}

