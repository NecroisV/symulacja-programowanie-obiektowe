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
    private List<environmentalResource> resources = new ArrayList<>();
    private int actualTick = 0;
    private final SimulationParameters parameters = SimulationParameters.getInstance();
    private DataCollector data = new DataCollector();
    private List<SafeZone> zones = new ArrayList<>();
    private final List<String> turnLogs = new ArrayList<>();
    private Render render = new Render();
    private String timeOfDay;

    private final EquipmentSpawnStrategy equipmentSpawnStrategy;

    public SimulationEnvironment(int width, int height){
        SimulationParameters.setProfile(3); // ustawienie profilu pod pokaz
        parameters.setSeed(128);            // ustawienie seeda

        this.equipmentSpawnStrategy = this::spawnEquipmentRandomly;
        createBoard(width, height);         // 1. tworzy pola i ściany (cellular automata)
        createSafeZones(width, height);     // 2. tworzy strefy bezpieczeństwa
        createAgents(parameters.getAgentsAmount()[0], parameters.getAgentsAmount()[1]);
        spawnEquipmentOnBoard();
        createResources(parameters.getResourceCount());
        data.updateData(this);
    }

    public boolean simulationStep(GraphicsContext gc, double tileSize){
        considerRandomEvent();
        moveAgents();
        considerInteractions();
        deleteDeadAgents();
        updateTimeOfDay();

        data.updateData(this);
        render.renderBoard(gc, tileSize, turnLogs, data, actualTick, board, EventManager.getCurrentEvent(), timeOfDay);

        return data.getSurvivorAmount() != 0;
    }

    public List<Agent> getAgentList() { return agentList; }
    public int getActualTick() { return this.actualTick; }

    private void updateTimeOfDay(){
        actualTick++;
        for(environmentalResource resource : resources){
            if(resource.wasUsed()) {
                resource.updateTime();
                resource.Respawn();
            }
        }
        for(Agent a : agentList){
            a.ageUp();
        }

        if(TimeOfDay.getVisibilityLevel(actualTick) < 0.57) timeOfDay = "Noc";
        else timeOfDay = "Dzień";
    }

    private static final int INITIAL_WALL_CHANCE = 35; // % szansa na ścianę przy inicjalizacji
    private static final int SMOOTHING_STEPS = 6;      // liczba iteracji wygładzania
    private static final int BIRTH_THRESHOLD = 5;      // min sąsiadów żeby pozostać/stać się ścianą
    private static final int DEATH_THRESHOLD = 3;      // max sąsiadów żeby pozostać wolnym polem

    private void createBoard(int width, int height){
        board = new Space[height][width];

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                board[y][x] = new Space(x, y);
                if(RNG.nextInt(100) < INITIAL_WALL_CHANCE){
                    board[y][x].createWall();
                }
            }
        }

        for(int step = 0; step < SMOOTHING_STEPS; step++){
            boolean[][] nextWalls = new boolean[height][width];
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    int wallNeighbours = countWallNeighbours(x, y, width, height);
                    if(board[y][x].isItWall()){
                        // ściana przeżywa jeśli ma wystarczająco dużo sąsiadów
                        nextWalls[y][x] = wallNeighbours >= DEATH_THRESHOLD;
                    } else {
                        // wolne pole staje się ścianą jeśli otoczone ścianami
                        nextWalls[y][x] = wallNeighbours >= BIRTH_THRESHOLD;
                    }
                }
            }
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    if(nextWalls[y][x]) board[y][x].createWall();
                    else board[y][x].destroyWallSilent(); // destroyWall bez notyfikacji SafeZone
                }
            }
        }

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                if(x > 0){
                    board[y][x].joinLeft(board[y][x-1]);
                    board[y][x-1].joinRight(board[y][x]);
                }
                if(x == width - 1){
                    board[y][0].joinLeft(board[y][x]);
                    board[y][x].joinRight(board[y][0]);
                }
            }
            if(y > 0){
                for(int x = 0; x < width; x++){
                    board[y][x].joinUp(board[y-1][x]);
                    board[y-1][x].joinDown(board[y][x]);
                }
            }
            if(y == height - 1){
                for(int x = 0; x < width; x++){
                    board[0][x].joinUp(board[y][x]);
                    board[y][x].joinDown(board[0][x]);
                }
            }
        }
    }

    private int countWallNeighbours(int x, int y, int width, int height){
        int count = 0;
        for(int dy = -1; dy <= 1; dy++){
            for(int dx = -1; dx <= 1; dx++){
                if(dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                // krawędź planszy traktujemy jako ścianę
                if (nx < 0) nx = width-1;
                if (nx >= width) nx = 0;
                if (ny < 0) ny = height-1;
                if (ny >= height) ny = 0;
                if(board[ny][nx].isItWall()){
                    count++;
                }
            }
        }
        return count;
    }




    private void createSafeZones(int width, int height){
        int safeZoneCount = parameters.getSafeZoneCount();
        int safeZoneSize = parameters.getSafeZoneSize();
        float healChance = parameters.getHealChance();
        float destructionThreshold = parameters.getDestructionThreshold();

        int attempts = 0;
        int created = 0;

        while(created < safeZoneCount && attempts < 100){
            attempts++;

            int margin = safeZoneSize + 4; // zwiększony margines z powodu czyszczenia 2 pól
            int startX = RNG.nextInt(width - margin * 2) + margin;
            int startY = RNG.nextInt(height - margin * 2) + margin;

            if(safeZoneOverlaps(startX, startY)){
                continue;
            }

            SafeZone zone = new SafeZone(healChance, destructionThreshold);

            // Tworzenie safe zone (ze ścianami na obwodzie)
            for(int dy = -1; dy <= safeZoneSize; dy++){
                for(int dx = -1; dx <= safeZoneSize; dx++){
                    int nx = startX + dx;
                    int ny = startY + dy;
                    if(nx < 0 || nx >= width || ny < 0 || ny >= height) continue;

                    Space space = board[ny][nx];
                    boolean isPerimeter = (dx == -1 || dx == safeZoneSize
                            || dy == -1 || dy == safeZoneSize);

                    if(isPerimeter){
                        // obwód = ściana
                        space.createWall();
                    } else {
                        // wnętrze = wolne pole
                        space.destroyWallSilent();
                    }
                    zone.addSpace(space);
                }
            }

            for(int dy = -3; dy <= safeZoneSize + 2; dy++){
                for(int dx = -3; dx <= safeZoneSize + 2; dx++){
                    int nx = startX + dx;
                    int ny = startY + dy;

                    // Sprawdź czy pole jest w granicach planszy
                    if(nx < 0 || nx >= width || ny < 0 || ny >= height) continue;

                    // Sprawdź czy to nie jest pole należące do safe zone
                    boolean isPartOfSafeZone = false;
                    for(int szY = -1; szY <= safeZoneSize; szY++){
                        for(int szX = -1; szX <= safeZoneSize; szX++){
                            if(nx == startX + szX && ny == startY + szY){
                                isPartOfSafeZone = true;
                                break;
                            }
                        }
                        if(isPartOfSafeZone) break;
                    }

                    if(!isPartOfSafeZone && board[ny][nx].isItWall()){
                        board[ny][nx].destroyWallSilent();
                    }
                }
            }

            int centerX = startX + safeZoneSize / 2;
            int centerY = startY + safeZoneSize / 2;

            // Wejście północne (górna ściana)
            int northX = centerX;
            int northY = startY - 1;
            if(northX >= 0 && northX < width && northY >= 0 && northY < height){
                if(board[northY][northX].isItWall()){
                    board[northY][northX].destroyWallSilent();
                }
            }

            // Wejście południowe (dolna ściana)
            int southX = centerX;
            int southY = startY + safeZoneSize;
            if(southX >= 0 && southX < width && southY >= 0 && southY < height){
                if(board[southY][southX].isItWall()){
                    board[southY][southX].destroyWallSilent();
                }
            }

            // Wejście zachodnie (lewa ściana)
            int westX = startX - 1;
            int westY = centerY;
            if(westX >= 0 && westX < width && westY >= 0 && westY < height){
                if(board[westY][westX].isItWall()){
                    board[westY][westX].destroyWallSilent();
                }
            }

            // Wejście wschodnie (prawa ściana)
            int eastX = startX + safeZoneSize;
            int eastY = centerY;
            if(eastX >= 0 && eastX < width && eastY >= 0 && eastY < height){
                if(board[eastY][eastX].isItWall()){
                    board[eastY][eastX].destroyWallSilent();
                }
            }

            zone.countWalls();
            zones.add(zone);
            created++;
        }
    }

    // sprawdza czy nowa strefa nakłada się na istniejące
    private boolean safeZoneOverlaps(int startX, int startY){
        int safeZoneSize = parameters.getSafeZoneSize();
        for(SafeZone zone : zones){
            for(Space space : zone.getCoveredSpaces()){
                int[] pos = space.getPosition();
                int px = pos[0];
                int py = pos[1];
                if(px >= startX - safeZoneSize - 4 && px <= startX + safeZoneSize * 2 + 4
                        && py >= startY - safeZoneSize - 4 && py <= startY + safeZoneSize * 2 + 4){
                    return true;
                }
            }
        }
        return false;
    }

    private void healSurvivorsInSafeZones(){
        List<SafeZone> expiredZones = new ArrayList<>();

        for(SafeZone zone : zones){
            if(zone.updateAndCheckExpiry()){
                expiredZones.add(zone);
                turnLogs.add("Strefa bezpieczeństwa została zniszczona!");
                continue;
            }
            for(Space space : new ArrayList<>(zone.getCoveredSpaces())){
                if(space.isItWall()) continue;
                for(Agent agent : space.getAgents()){
                    if(agent instanceof Survivor && agent.isItAlive() && !((Survivor) agent).isStarving()){
                        zone.healSurvivor((Survivor) agent);
                        data.incHealedWoundInSafeZones();
                    }
                }
            }
        }

        zones.removeAll(expiredZones);
    }

    private void createResources(int resourceNumber){
        int width = board[0].length;
        int height = board.length;
        int i = 0;
        while(i < resourceNumber){
            int x = RNG.nextInt(width);
            int y = RNG.nextInt(height);
            if (!board[y][x].isItWall()&& !board[y][x].isInSafeZone() && !board[y][x].containsResource()){
                environmentalResource resource = new environmentalResource();
                board[y][x].addResource(resource);
                resources.add(resource);
                i++;
            }
        }
    }

    private void createAgents(int survivorNumber, int infectedNumber){
        int width = board[0].length;
        int height = board.length;
        int i = 0;

        int[] survivorStats = parameters.getSurvivorStats();
        int[] infectedStats = parameters.getInfectedStats();
        int[] EqAndWoundChances = parameters.getEqAndWoundChances();

        // Tworzenie ocalałych
        while(i < survivorNumber){
            int x = RNG.nextInt(width - 1);
            int y = RNG.nextInt(height - 1);
            if (!board[y][x].isItWall() && !board[y][x].isInSafeZone()){
                Survivor survivor = new Survivor(x, y, survivorStats[0], survivorStats[1], survivorStats[2], survivorStats[3]);
                if(RNG.nextInt(100) > EqAndWoundChances[0]){
                    survivor.getEquipment(EquipmentFactory.createRandom(EquipmentFactory.EquipmentType.WEAPON));
                }
                agentList.add(survivor);
                board[y][x].addAgent(survivor);
                i++;
            }
        }

        // Tworzenie zakażonych
        i = 0;
        while(i < infectedNumber){
            int x = RNG.nextInt(width - 1);
            int y = RNG.nextInt(height - 1);
            if (!board[y][x].isItWall() && !board[y][x].isInSafeZone()){
                Infected infected = new Infected(x, y, infectedStats[0], infectedStats[1], infectedStats[2], infectedStats[3]);
                if(RNG.nextInt(100) > EqAndWoundChances[1]){
                    infected.reviveWound();
                }
                agentList.add(infected);
                board[y][x].addAgent(infected);
                i++;
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
                            if(!usedAgentList.contains(a) && a.isItAlive()) {
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
                if (!space.isItWall()) {
                    List<Agent> agentsOnSpace = new ArrayList<>(space.getAgents());
                    List<Survivor> survivors = new ArrayList<>();
                    List<Infected> infected = new ArrayList<>();

                    for (Agent a : agentsOnSpace) {
                        if (a.isItAlive()) {
                            if (a instanceof Survivor) survivors.add((Survivor) a);
                            else if (a instanceof Infected) infected.add((Infected) a);
                        }
                    }

                    // walka Ocalały vs Ocalały — tylko poza SafeZone
                    if (infected.isEmpty() && survivors.size() > 1 && !space.isInSafeZone()) {
                        data.incSurvivorSurvivorInteractions();
                        while (survivors.size() > 1) {
                            Survivor s1 = survivors.get(0);
                            Survivor s2 = survivors.get(1);
                            int s1Weight = s1.calculateStrength();
                            int s2Weight = s2.calculateStrength();
                            int roll = RNG.nextInt(s1Weight + s2Weight);

                            if (roll < s1Weight) {
                                s2.changeHealthLevel(-Math.max(5, s1Weight - (s2Weight / 2)));
                                if (RNG.nextFloat() < parameters.getChanceForWoundAfterBattle()) s2.reviveWound();
                            } else {
                                s1.changeHealthLevel(-Math.max(5, s2Weight - (s1Weight / 2)));
                                if (RNG.nextFloat() < parameters.getChanceForWoundAfterBattle()) s1.reviveWound();
                            }

                            if (!s2.isItAlive() || s2.getHealth() <= 0) {
                                turnLogs.add("Ocalały pokonał innego ocalałego na pozycji [" + x + ", " + y + "]");
                                s1.steal(s2); s2.die(); space.deleteAgent(s2); survivors.remove(s2);
                            } else if (!s1.isItAlive() || s1.getHealth() <= 0) {
                                turnLogs.add("Ocalały pokonał innego ocalałego na pozycji [" + x + ", " + y + "]");
                                s2.steal(s1); s1.die(); space.deleteAgent(s1); survivors.remove(s1);
                            }
                        }
                    }

                    // walka Ocalały vs Zakażony
                    if (!survivors.isEmpty() && !infected.isEmpty()) {
                        data.incSurvivorInfectedInteractions();
                        while (!survivors.isEmpty() && !infected.isEmpty()) {
                            Survivor survivor = survivors.getFirst();
                            Infected zakazony = infected.getFirst();
                            int zakazonyWeight = zakazony.calculateStrength();
                            int survivorWeight = survivor.calculateStrength();
                            int roll = RNG.nextInt(zakazonyWeight + survivorWeight);

                            if (roll < zakazonyWeight) {
                                survivor.changeHealthLevel(-Math.max(5, zakazonyWeight - (survivorWeight / 2)));
                                if (RNG.nextFloat() < parameters.getHealChance()) zakazony.changeHealthLevel(5);
                                if (RNG.nextFloat() < parameters.getChanceForWoundAfterBattle()) survivor.reviveWound();
                                if (RNG.nextFloat() < parameters.getInfectionChance()) {
                                    boolean isPrevented = false;
                                    for(Equipment eq : survivor.getEquipment()){
                                        if(eq instanceof Clothes && ((Clothes) eq).getInfectionPrevention()){
                                            isPrevented = true;
                                        }
                                    }
                                    if(!isPrevented){ transformSurvivor(survivor, zakazony, x, y); }
                                }
                            } else {
                                zakazony.changeHealthLevel(-Math.max(5, survivorWeight - (zakazonyWeight / 2)));
                                if (RNG.nextFloat() < parameters.getChanceForWoundAfterBattle()) zakazony.reviveWound();
                            }

                            if (!zakazony.isItAlive() || zakazony.getHealth() <= 0) {
                                turnLogs.add("Zakażony na pozycji [" + x + ", " + y + "] został zlikwidowany!");
                                zakazony.die(); space.deleteAgent(zakazony); infected.remove(zakazony);
                            }
                            if (!survivor.isItAlive() || survivor.getHealth() <= 0) {
                                turnLogs.add("Ocalały na pozycji [" + x + ", " + y + "] poległ w walce.");
                                survivor.die(); space.deleteAgent(survivor); survivors.remove(survivor);
                            }
                        }
                    }

                    // podnoszenie ekwipunku i zasobów
                    List<Agent> afterFight = new ArrayList<>(space.getAgents());
                    List<Survivor> survivorsAfter = new ArrayList<>();
                    for (Agent a : afterFight) {
                        if (a.isItAlive() && a instanceof Survivor) survivorsAfter.add((Survivor) a);
                    }

                    if(!survivorsAfter.isEmpty() && space.hasEquipment()) {
                        survivorsAfter.getFirst().pickUpEquipment(space);
                        int[] position = space.getPosition();
                        turnLogs.add("Ocalały na pozycji [" + position[0] + ", " + position[1] + "] podniósł ekwipunek");
                    }
                    if(!survivorsAfter.isEmpty() && space.containsResource()){
                        int[] restoredThings = space.getResource().getUsed();
                        survivorsAfter.getFirst().changeEnergyLevel(restoredThings[0]);
                        survivorsAfter.getFirst().changeHealthLevel(restoredThings[1]);
                        int[] position = space.getPosition();
                        turnLogs.add("Ocalały na pozycji [" + position[0] + ", " + position[1] + "] zebrał zasoby");
                    }
                }
            }
        }

        // leczenie w SafeZone — po wszystkich interakcjach
        healSurvivorsInSafeZones();
    }

    private void transformSurvivor(Survivor o, Infected z, int x, int y) {
        Infected newInfected = o.transformIntoInfected(z);
        turnLogs.add("Ocalały na pozycji [" + x + ", " + y + "] zmienił się w Zakażonego!");
        o.die();
        board[y][x].deleteAgent(o);
        agentList.add(newInfected);
        board[y][x].addAgent(newInfected);
    }

    private void deleteDeadAgents() {
        int width = board[0].length;
        int height = board.length;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Space space = board[y][x];
                if (!space.isItWall()) {
                    List<Agent> agentsOnSpace = new ArrayList<>(space.getAgents());
                    for (Agent a : agentsOnSpace) {
                        if (!a.isItAlive()) space.deleteAgent(a);
                    }
                }
            }
        }
        agentList.removeIf(a -> !a.isItAlive());
    }

    private void considerRandomEvent(){
        EventManager.runEventCheck(this.board);
    }

    private void spawnEquipmentOnBoard() {
        int weaponCount = parameters.getWeaponCount();
        int clothesCount = parameters.getClothesCount();
        equipmentSpawnStrategy.spawnEquipment(board, weaponCount, clothesCount);
    }

    private void spawnItems(List<Space> freeSpaces, int count, EquipmentFactory.EquipmentType type){
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
                if (!space.isItWall() && !space.isInSafeZone()) {
                    freeSpaces.add(space);
                }
            }
        }
        spawnItems(freeSpaces, weaponCount, EquipmentFactory.EquipmentType.WEAPON);
        spawnItems(freeSpaces, clothesCount, EquipmentFactory.EquipmentType.CLOTHES);
    }

    public Space[][] getBoard() { return board; }
}
