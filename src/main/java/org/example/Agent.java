package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Agent {
    private int x;
    private int y;
    private int age = 0;
    private int healthLevel;
    private int maxHealthLevel;
    private int baseStrength;
    private int baseFOV = 5;
    private int baseSpeed = 2;
    private boolean isAlive = true;
    private List<Wound> wounds = new ArrayList<>();

    // Konstruktor agenta - ustawia pozycję, zdrowie, siłę, pole widzenia i prędkość
    protected Agent(int given_x, int given_y, int given_health, int given_baseStrength, int given_baseFOV, int given_baseSpeed){
        x = given_x;
        y = given_y;
        healthLevel = given_health;
        maxHealthLevel = given_health;
        baseStrength = given_baseStrength;
        baseFOV = given_baseFOV;
        baseSpeed = given_baseSpeed;
    }

    // Abstrakcyjna metoda obliczająca wagi dla pól - implementowana przez Survivor i Infected
    public abstract void getAgentWeights(Space start, Map<String, Integer> weights, int divisor);

    // Wykonuje ruch agenta na podstawie obliczonych wag
    public int[] makeMove(Space start, Map<String, Integer> weights, int divisor) {
        getAgentWeights(start, weights, divisor);
        ArrayList<Space> availableSpaces = possibleMove(start);

        if (availableSpaces.isEmpty()) return start.getPosition();
        if (this instanceof Survivor){((Survivor) this).changeEnergyLevel(-5);}

        int totalWeightSum = 0;
        List<Integer> validWeights = new ArrayList<>();

        // Zbieranie ważonych dostępnych pól
        for (Space s : availableSpaces) {
            int w = Math.max(0, s.getWeight());
            validWeights.add(w);
            totalWeightSum += w;
        }

        int[] targetPosition;

        // Jeśli wszystkie wagi są zerowe lub ujemne - wybierz pole z najwyższą wagą
        if (totalWeightSum <= 0) {
            Space bestWorst = availableSpaces.getFirst();
            for(Space s : availableSpaces){
                if(s.getWeight() > bestWorst.getWeight()) {
                    bestWorst = s;
                }
            }
            targetPosition = bestWorst.getPosition();
        } else {
            // Losowy wybór proporcjonalny do wag
            int rolledValue = RNG.nextInt(totalWeightSum);
            int currentSum = 0;
            int selectedIndex = availableSpaces.size() - 1;
            for (int i = 0; i < availableSpaces.size(); i++) {
                currentSum += validWeights.get(i);
                if (rolledValue < currentSum) {
                    selectedIndex = i;
                    break;
                }
            }
            targetPosition = availableSpaces.get(selectedIndex).getPosition();
        }

        // Resetowanie wag po wykonaniu ruchu
        for (Space s : getLocalArea(start)) {
            s.changeWeight(-s.getWeight());
        }

        return targetPosition;
    }

    // Dodaje wagę do pola i rozlewa jej część na sąsiednie pola
    protected void addWeightWithSpill(Space s, int weight, int divisor) {
        if (s == null || s.isItWall()) return;
        s.changeWeight(weight);
        if (divisor <= 1) return;
        int spilled = weight / divisor;
        Space[] n = {s.getUp(), s.getRight(), s.getDown(), s.getLeft()};
        for (Space neighbor : n){
            if (neighbor != null && !neighbor.isItWall()){
                neighbor.changeWeight(spilled);
            }
        }
    }

    // Pobiera wszystkie pola w zadanym promieniu (BFS)
    protected ArrayList<Space> getSpacesWithinRadius(Space start, int radius, boolean ignoreWalls) {
        if(this instanceof Survivor){
            ((Survivor) this).changeEnergyLevel(-5);
        }
        ArrayList<Space> visitedSpaces = new ArrayList<>();
        if (start == null || (start.isItWall() && !ignoreWalls)) return visitedSpaces;

        ArrayList<Space> que = new ArrayList<>();
        ArrayList<Integer> distances = new ArrayList<>();

        que.add(start);
        distances.add(0);
        visitedSpaces.add(start);

        int analysedIndex = 0;

        // BFS do promienia radius
        while (analysedIndex < que.size()) {
            Space actual = que.get(analysedIndex);
            int actualDistance = distances.get(analysedIndex);
            analysedIndex++;

            if (actualDistance >= radius) continue;

            Space[] neighbours = {actual.getUp(), actual.getRight(), actual.getDown(), actual.getLeft()};

            for (Space n : neighbours) {
                boolean canPass = ignoreWalls || !n.isItWall();
                if (n != null && canPass && !visitedSpaces.contains(n)) {
                    que.add(n);
                    distances.add(actualDistance + 1);
                    visitedSpaces.add(n);
                }
            }
        }
        return visitedSpaces;
    }

    // Zwraca pola, na które agent może się poruszyć (uwzględnia prędkość)
    private ArrayList<Space> possibleMove(Space start) {
        return getSpacesWithinRadius(start, this.calculateSpeed(), false);
    }

    // Zwraca lokalny obszar wokół agenta (FOV + prędkość)
    protected List<Space> getLocalArea(Space start) {
        int actionRadius = calculateFOV() + calculateSpeed();
        return getSpacesWithinRadius(start, actionRadius, false);
    }

    // Określa, co agent widzi - uwzględnia linię widzenia (ściany blokują)
    public ArrayList<ArrayList<Space>> whatAgentSaw(Space start){
        int actualFOV = calculateFOV();
        ArrayList<Space> seenSpaces = getSpacesWithinRadius(start, actualFOV, true);

        // Mapa do szybkiego dostępu do pól
        Map<String, Space> localGridMap = new HashMap<>();
        for (Space s : seenSpaces) {
            int[] pos = s.getPosition();
            localGridMap.put(pos[0] + "," + pos[1], s);
        }

        ArrayList<Space> trulySeenResourceList = new ArrayList<>();
        ArrayList<Space> trulySeenAgentList = new ArrayList<>();
        ArrayList<Space> trulySeenEquipmentList = new ArrayList<>();

        // Filtrowanie tylko pól z linią widzenia
        for (Space space : seenSpaces) {
            if (hasLineOfSight(start, space, localGridMap)) {
                if (space.containsResource()) trulySeenResourceList.add(space);
                if (space.containsAgents()) trulySeenAgentList.add(space);
                if (space.hasEquipment()) trulySeenEquipmentList.add(space);
            }
        }

        ArrayList<ArrayList<Space>> seenThing = new ArrayList<>();
        seenThing.add(trulySeenResourceList);
        seenThing.add(trulySeenAgentList);
        seenThing.add(trulySeenEquipmentList);

        return seenThing;
    }

    // Sprawdza linię widzenia między dwoma polami (algorytm Bresenhama)
    private boolean hasLineOfSight(Space start, Space target, Map<String, Space> localGridMap) {
        int[] pos0 = start.getPosition();
        int[] pos1 = target.getPosition();

        int x0 = pos0[0];
        int y0 = pos0[1];
        int x1 = pos1[0];
        int y1 = pos1[1];

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            Space currentSpace = localGridMap.get(x0 + "," + y0);

            // Sprawdź czy pole nie jest ścianą (pomijamy pole startowe)
            if ((x0 != pos0[0] || y0 != pos0[1])) {
                if (currentSpace == null || currentSpace.isItWall()) {
                    return false;
                }
            }

            if (x0 == x1 && y0 == y1) {
                break;
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
        return true;
    }

    // Zwiększa wiek agenta o 1 tick
    public void ageUp(){
        age++;
    }

    // Zabija agenta
    public void die() {
        this.isAlive = false;
        this.healthLevel = 0;
    }

    // Zmienia poziom zdrowia - uwzględnia redukcję obrażeń z ubrań dla ocalałych
    public void changeHealthLevel(int amount) {
        if(this instanceof Survivor && amount < 0){
            for(Equipment equipment : ((Survivor) this).getEquipment()){
                if(equipment instanceof Clothes){
                    amount += ((Clothes) equipment).getDamageReduction();
                }
            }
        }
        this.healthLevel += amount;
        // Przy leczeniu jest szansa na wyleczenie rany
        if (amount > 0){
            if (RNG.nextFloat(1)>SimulationParameters.getInstance().getHealChance()) healWound();
        }
        if (this.healthLevel <= 0) {
            die();
        }
        if(healthLevel>maxHealthLevel) healthLevel = maxHealthLevel;
    }

    // Oblicza aktualne pole widzenia - uwzględnia porę dnia i rany głowy
    public int calculateFOV(){
        int currentFOV = (int) (baseFOV * TimeOfDay.getVisibilityLevel(SimulationApp.getEnvironment().getActualTick()));
        for(Wound wound : wounds){
            if(wound instanceof HeadWound){
                currentFOV -= ((HeadWound) wound).getReduction();
            }
        }
        return Math.max(0, currentFOV);
    }

    // Oblicza aktualną siłę - uwzględnia broń, głód i rany rąk
    public int calculateStrength(){
        int currentStrength = baseStrength;
        for(Wound wound : wounds){
            if(wound instanceof ArmWound){
                currentStrength -= ((ArmWound) wound).getReduction();
            }
        }
        if (this instanceof Survivor){
            if (((Survivor) this).isStarving()) currentStrength = (int) currentStrength/2;
            for(Equipment equipment : ((Survivor) this).getEquipment()){
                if (equipment instanceof Weapon){
                    currentStrength += ((Weapon) equipment).calculateActualStrength();
                    ((Weapon) equipment).loseDurability(); // Broń traci wytrzymałość przy użyciu
                }
            }
        }
        return Math.max(1, currentStrength);
    }

    // Oblicza aktualną prędkość - uwzględnia rany nóg
    public int calculateSpeed(){
        int currentSpeed = baseSpeed;
        for(Wound wound : wounds){
            if(wound instanceof LegWound){
                currentSpeed -= ((LegWound) wound).getReduction();
            }
        }
        return Math.max(0, currentSpeed);
    }

    public boolean isItAlive(){return isAlive;}
    public int[] getPosition(){ return new int[]{x, y}; }
    public int getHealth(){return healthLevel;}

    // Leczy losową ranę
    public void healWound() {
        if (wounds.isEmpty()) return;
        wounds.remove(RNG.nextInt(wounds.size()));
    }

    // Zadaje losową ranę (głowy, ręki lub nogi)
    public void reviveWound(){
        switch (RNG.nextInt(3)){
            case(0) -> wounds.add(new HeadWound());
            case(1) -> wounds.add(new ArmWound());
            case(2) -> wounds.add(new LegWound());
        }
    }

    public int getAge(){return age;}
}