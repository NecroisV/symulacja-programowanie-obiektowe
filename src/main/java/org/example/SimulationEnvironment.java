package org.example;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

public class SimulationEnvironment {
    private Space[][] board;
    private List<Agent> agentList = new ArrayList<>();
    private Set<Agent> usedAgentList = new HashSet<>();
    private int actualTick = 0;
    private float lightLevel;
    private TimeOfDay timeOfDay;
    private final SimulationParameters parameters = new SimulationParameters();

    private DataCollector data = new DataCollector();

    private List<SafeZone> zones = new ArrayList<>();
    private List<RandomEvent> randomEvents = new ArrayList<>();
    private final List<String> turnLogs = new ArrayList<>();

    public SimulationEnvironment(int width, int height){
        createBoard(width, height);
        int[] agentNumbers = parameters.getAgentsAmount();
        int[] chances = parameters.getEqAndWoundChances();
        createAgents(agentNumbers[0], agentNumbers[1], chances[0], chances[1]);

        data.updateData(this);
    }

    public void simulationStep(GraphicsContext gc, double tileSize){
        considerRandomEvent();
        moveAgents();
        considerInteractions();
        deleteDeadAgents();
        updateTimeOfDay();

        data.updateData(this);

        render(gc, tileSize);
    }

    public List<Agent> getAgentList() { return agentList; }
    public int getActualTick() { return actualTick; }

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
                if(random.nextInt(100)>=90) {
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
                Survivor survivor = new Survivor(x, y, health, board);
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
                Infected infected = new Infected(x, y, health, board);
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
        java.util.Random rand = new java.util.Random();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Space space = board[y][x];

                if (space != null && !space.isItWall() && space.getAgents().size() > 1) {
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

                            int roll = rand.nextInt(totalWeight);

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
                            }
                            else if (!s1.isItAlive() || s1.getHealth() <= 0) {
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
                            Survivor ocalały = survivors.get(0);
                            Infected zombi = infected.get(0);

                            int zombiWeight = Math.max(1, zombi.calculateStrength());
                            int survivorWeight = Math.max(1, ocalały.calculateStrength());
                            int totalWeight = zombiWeight + survivorWeight;

                            int roll = rand.nextInt(totalWeight);

                            if (roll < zombiWeight) {
                                int damage = Math.max(5, zombiWeight - (survivorWeight / 2));
                                ocalały.changeHealthLevel(-damage);
                            } else {
                                int damage = Math.max(5, survivorWeight - (zombiWeight / 2));
                                zombi.changeHealthLevel(-damage);
                            }

                            if (!zombi.isItAlive() || zombi.getHealth() <= 0) {
                                turnLogs.add("Zakażony na pozycji [" + x + ", " + y + "] został permanentnie zlikwidowany!");
                                zombi.die();
                                space.deleteAgent(zombi);
                                infected.remove(zombi);
                            }

                            if (!ocalały.isItAlive() || ocalały.getHealth() <= 0) {
                                if (rand.nextFloat() < zombi.getInfectionChance()) {
                                    transformSurvivor(ocalały, zombi, x, y);
                                } else {
                                    turnLogs.add("Ocalały na pozycji [" + x + ", " + y + "] poległ w walce i zmarł.");
                                    ocalały.die();
                                    space.deleteAgent(ocalały);
                                }
                                survivors.remove(ocalały);
                            }
                        }
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
        // data.incHealedWoundInSafeZones();
    }

    private void render(GraphicsContext gc, double tileSize) {
        int width = board[0].length;
        int height = board.length;

        gc.setFill(Color.web("#1e1e24"));
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, tileSize * 0.5));

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double x = j * tileSize;
                double y = i * tileSize;
                Space space = board[i][j];

                if (space.isItWall()) {
                    gc.setFill(Color.web("#ff7e21"));
                    gc.fillRect(x, y, tileSize - 1, tileSize - 1);
                    gc.setFill(Color.web("#6c757d"));
                    gc.fillText("#", x + tileSize / 2, y + tileSize / 2);
                } else {
                    gc.setFill(Color.web("#2d2d35"));
                    gc.fillRect(x, y, tileSize - 1, tileSize - 1);

                    List<Agent> agentsOnSpace = space.getAgents();
                    if (!agentsOnSpace.isEmpty()) {
                        double circleRadius = tileSize * 0.8;
                        double offset = (tileSize - circleRadius) / 2;

                        if (agentsOnSpace.size() > 1) {
                            gc.setFill(Color.web("#d90429"));
                            gc.fillOval(x + offset, y + offset, circleRadius, circleRadius);
                            gc.setFill(Color.WHITE);
                            gc.fillText("X", x + tileSize / 2, y + tileSize / 2);
                        } else {
                            Agent a = agentsOnSpace.get(0);
                            if (a instanceof Survivor) {
                                gc.setFill(Color.web("#00b4d8"));
                                gc.fillOval(x + offset, y + offset, circleRadius, circleRadius);
                                gc.setFill(Color.WHITE);
                                gc.fillText("S", x + tileSize / 2, y + tileSize / 2);
                            } else if (a instanceof Infected) {
                                gc.setFill(Color.web("#38b000"));
                                gc.fillOval(x + offset, y + offset, circleRadius, circleRadius);
                                gc.setFill(Color.WHITE);
                                gc.fillText("Z", x + tileSize / 2, y + tileSize / 2);
                            }
                        }
                    }
                }
            }
        }

        double panelStartY = height * tileSize + 20;

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        gc.setFill(Color.WHITE);
        gc.fillText("TICK (TURA): " + actualTick, 15, panelStartY);

        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        gc.fillText("OSTATNIE WYDARZENIA:", 15, panelStartY + 30);

        double logY = panelStartY + 55;
        gc.setFont(Font.font("SansSerif", FontWeight.NORMAL, 13));
        if (turnLogs.isEmpty()) {
            gc.setFill(Color.LIGHTGREEN);
            gc.fillText("Brak incydentów w tej turze.", 20, logY);
        } else {
            gc.setFill(Color.web("#ffb703"));
            int displayedLogs = 0;
            for (String log : turnLogs) {
                if (displayedLogs >= 5) break;
                gc.fillText("• " + log, 20, logY);
                logY += 18;
                displayedLogs++;
            }
        }
        turnLogs.clear();

        double statsStartX = Math.max(380, width * tileSize * 0.55); // Automatyczne przesunięcie kolumny w prawo

        gc.setFill(Color.web("#25252d"));
        gc.fillRect(statsStartX - 10, panelStartY, 320, 150); // tło pod dashboard

        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#00b4d8"));
        gc.fillText("LIVE STATS DASHBOARD", statsStartX, panelStartY + 10);

        gc.setFont(Font.font("SansSerif", FontWeight.NORMAL, 13));
        gc.setFill(Color.WHITE);


        gc.fillText("Ocalali (S): " + data.getSurvivorAmount(), statsStartX, panelStartY + 35);
        gc.fillText("Zakażeni (Z): " + data.getInfectedAmount(), statsStartX, panelStartY + 53);


        gc.fillText("Starcia S ↔ Z: " + data.getSurvivorInfectedInteractions(), statsStartX, panelStartY + 71);
        gc.fillText("Spotkania S ↔ S: " + data.getSurvivorSurvivorInteractions(), statsStartX, panelStartY + 89);



        statsStartX += 350;
        gc.setFill(Color.web("#25252d"));
        gc.fillRect(statsStartX - 10, panelStartY, 320, 150);

        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#00b4d8"));
        gc.fillText("LIVE STATS DASHBOARD", statsStartX, panelStartY + 10);

        gc.setFont(Font.font("SansSerif", FontWeight.NORMAL, 13));
        gc.setFill(Color.WHITE);


        gc.fillText("Średnie zdrowie grupy: " + String.format("%.1f", data.getMeanHealth()) + " HP", statsStartX, panelStartY + 35);
        gc.fillText("Uleczenia w SafeZone: " + data.getHealedWoundInSafeZones(), statsStartX, panelStartY + 53);

        if (data.getTimeToSurvivorsExtinction() != -1) {
            gc.setFill(Color.web("#d90429"));
            gc.fillText("Zagłada ocalałych w turze: " + data.getTimeToSurvivorsExtinction(), statsStartX, panelStartY + 71);
        } else {
            gc.setFill(Color.LIGHTGREEN);
            gc.fillText("Status ludzkości: Aktywni", statsStartX, panelStartY + 89);
        }
    }
}