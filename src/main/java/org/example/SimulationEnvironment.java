package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationEnvironment {
    private Space[][] board;
    private List<Agent> agentList = new ArrayList<>();
    private List<Agent> usedAgentList = new ArrayList<>();
    private int actualTick = 0;
    private float lightLevel;
    private TimeOfDay timeOfDay;
    private SimulationParameters parameters = SimulationParameters.getInstance();
    private DataCollector data;
    private List<SafeZone> zones = new ArrayList<>();
    private EquipmentSpawnStrategy equipmentSpawnStrategy;

    public SimulationEnvironment(int width, int height){
        this.equipmentSpawnStrategy = this::spawnEquipmentRandomly;
        createBoard(width, height);
        int[] agentNumbers = parameters.getAgentsAmount();
        int[] chances = parameters.getEqAndWoundChances();
        createAgents(agentNumbers[0], agentNumbers[1], chances[0], chances[1]);
        spawnEquipmentOnBoard();
    }

    public void simulationStep(){
        considerRandomEvent();
        moveAgents();
        showBoard();
        considerInteractions();
        deleteDeadAgents();
        updateTimeOfDay();
    }

    private void updateTimeOfDay(){
        calculateLightLevel();
        actualTick++;
    }

    private void calculateLightLevel(){
        lightLevel = 1.0f;
    }

    private void createBoard(int width, int height){
        board = new Space[height][width];
        Random random = new Random();
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
                if(random.nextInt(100)>=70) {
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

        Random random = new Random();

        int health = 100;

        int i = 0;
        while(i < survivorNumber){
            int x = random.nextInt(width -1);
            int y = random.nextInt(height -1);

            if (!board[y][x].isItWall()){
                Survivor survivor = new Survivor(x, y, health);
                agentList.add(survivor);
                board[y][x].addAgent(survivor);
                i ++;
            }
        }

        i = 0;
        while(i < infectedNumber){
            int x = random.nextInt(width -1);
            int y = random.nextInt(height -1);

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

                    List<Agent> copyAgents = new ArrayList<>(originalAgents);

                    for(Agent a : copyAgents){
                        if(!usedAgentList.contains(a)) {
                            usedAgentList.add(a);
                            int[] agentMove = a.makeMove(board[i][j]);
                            displaceAgent(new int[]{i, j}, agentMove, a);
                            int newX = agentMove[0];
                            int newY = agentMove[1];
                            if(a instanceof Survivor && board[newY][newX].hasEquipment()) {
                                a.pickUpEquipment(board[newY][newX]);
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

    private void considerInteractions(){

    }

    private void transformSurvivor(Survivor o, Infected z){

    }

    private void deleteDeadAgents(){
        for (int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[0].length; j++){
                Space space = board[i][j];
                if(!space.isItWall()){
                    space.getAgents().removeIf(agent-> !agent.isItAlive());
                }
            }
        }
    }

    private void considerRandomEvent(){
        EventManager.runEventCheck(this.board);
    }

    private void showBoard() {
        int width = board[0].length;
        int height = board.length;

        //to clean the preview
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println("\n--- TICK: " + actualTick + " ---");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Space space = board[i][j];
                if (space.isItWall()) {
                    System.out.print("# ");
                } else {
                    List<Agent> agentsOnSpace = space.getAgents();
                    if (agentsOnSpace.isEmpty()) {
                        if(space.hasEquipment()) {
                            System.out.print("E ");
                        }
                        else {
                            System.out.print(". ");
                        }
                    } else if (agentsOnSpace.size() > 1) {
                        System.out.print(agentsOnSpace.size() + " ");
                    } else {
                        Agent a = agentsOnSpace.getFirst();
                        if (a instanceof Survivor) {
                            System.out.print("O ");
                        } else if (a instanceof Infected) {
                            System.out.print("Z ");
                        } else {
                            System.out.print("? ");
                        }
                    }
                }
            }
            System.out.println();
        }

    }
    private void spawnEquipmentOnBoard() {
        int weaponCount = parameters.getWeaponCount();
        int clothesCount = parameters.getClothesCount();
        equipmentSpawnStrategy.spawnEquipment(board, weaponCount, clothesCount);
    }
    private void spawnItems(List<Space> freeSpaces, int count,
                            EquipmentFactory.EquipmentType type, Random random) {
        int spawned = 0;
        int attempts = 0;
        while (spawned < count && attempts < freeSpaces.size()) {
            Space space = freeSpaces.get(random.nextInt(freeSpaces.size()));
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
        Random random = new Random();
        spawnItems(freeSpaces, weaponCount, EquipmentFactory.EquipmentType.WEAPON, random);
        spawnItems(freeSpaces, clothesCount, EquipmentFactory.EquipmentType.CLOTHES, random);
    }

    public Space[][] getBoard() {
        return board;
    }
}

