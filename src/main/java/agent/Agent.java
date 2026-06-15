package agent;

import equipment.Clothes;
import equipment.Equipment;
import equipment.Weapon;
import event.TimeOfDay;
import simulation.RNG;
import simulation.SimulationApp;
import simulation.SimulationParameters;
import simulation.Space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Klasa abstrakcyjna reprezentująca autonomicznego agenta w symulacji.
 * Odpowiada za podstawowe mechaniki poruszania się, percepcji otoczenia,
 * walki oraz zarządzania stanem zdrowia i ranami.
 */
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

    /**
     * Konstruktor agenta - inicjalizuje podstawowe parametry życiowe i fizyczne.
     *
     * @param given_x            Początkowa współrzędna X.
     * @param given_y            Początkowa współrzędna Y.
     * @param given_health       Maksymalny i początkowy poziom zdrowia.
     * @param given_baseStrength Bazowa siła agenta.
     * @param given_baseFOV      Bazowe pole widzenia (promień).
     * @param given_baseSpeed     Bazowa prędkość (zasięg ruchu).
     */
    protected Agent(int given_x, int given_y, int given_health, int given_baseStrength, int given_baseFOV, int given_baseSpeed){
        x = given_x;
        y = given_y;
        healthLevel = given_health;
        maxHealthLevel = given_health;
        baseStrength = given_baseStrength;
        baseFOV = given_baseFOV;
        baseSpeed = given_baseSpeed;
    }

    /**
     * Abstrakcyjna metoda obliczająca wagi dla pól w otoczeniu agenta.
     * Musi być zaimplementowana przez klasy pochodne (np. Survivor, Infected).
     *
     * @param start   Pole startowe (aktualna pozycja).
     * @param weights Mapa wag przypisanych do konkretnych zdarzeń/obiektów.
     * @param divisor Dzielnik używany do osłabiania wpływu wagi wraz z odległością.
     */
    public abstract void getAgentWeights(Space start, Map<String, Integer> weights, int divisor);

    /**
     * Wykonuje ruch agenta na podstawie obliczonych wag pól w jego otoczeniu.
     * Wybór pola docelowego opiera się na algorytmie losowania proporcjonalnego do wag.
     *
     * @param start   Pole startowe (aktualna pozycja).
     * @param weights Mapa wag dla algorytmu decyzyjnego.
     * @param divisor Dzielnik rozlewania wag.
     * @return Tablica dwuelementowa int[] zawierająca nowe współrzędne [x, y] agenta.
     */
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

    /**
     * Dodaje wagę do wskazanego pola i rozlewa jej osłabioną część na sąsiednie pola (góra, dół, lewo, prawo).
     *
     * @param s       Pole centralne, do którego dodawana jest główna waga.
     * @param weight  Wartość dodawanej wagi.
     * @param divisor Dzielnik określający, jak bardzo waga słabnie na sąsiednich polach.
     */
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

    /**
     * Pobiera wszystkie pola w zadanym promieniu, wykorzystując algorytm przeszukiwania wszerz (BFS).
     *
     * @param start       Pole, od którego zaczyna się przeszukiwanie.
     * @param radius      Maksymalny promień przeszukiwania (odległość).
     * @param ignoreWalls Flaga określająca, czy algorytm ma ignorować ściany przy przechodzeniu pól.
     * @return Lista {@link ArrayList} obiektów {@link Space} znajdujących się w danym promieniu.
     */
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

    /**
     * Zwraca listę pól, na które agent jest w stanie fizycznie się poruszyć w tej turze (uwzględnia prędkość).
     *
     * @param start Pole startowe.
     * @return Lista dostępnych pól.
     */
    private ArrayList<Space> possibleMove(Space start) {
        return getSpacesWithinRadius(start, this.calculateSpeed(), false);
    }

    /**
     * Zwraca lokalny obszar wokół agenta, w którym operuje (zasięg pola widzenia + prędkość).
     *
     * @param start Pole startowe.
     * @return Lista pól w obszarze lokalnym.
     */
    protected List<Space> getLocalArea(Space start) {
        int actionRadius = calculateFOV() + calculateSpeed();
        return getSpacesWithinRadius(start, actionRadius, false);
    }

    /**
     * Określa, jakie obiekty (zasoby, agenci, ekwipunek) agent faktycznie widzi.
     * Uwzględnia ograniczenie przez przeszkody (ściany) za pomocą linii widzenia.
     *
     * @param start Pole, na którym stoi agent.
     * @return Lista list zawierająca kolejno: widoczne zasoby, widocznych agentów oraz widoczny ekwipunek.
     */
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

    /**
     * Sprawdza, czy istnieje bezpośrednia linia widzenia (brak ścian) między dwoma polami.
     * Wykorzystuje zmodyfikowany algorytm rysowania linii Bresenhama.
     *
     * @param start        Pole początkowe (oczy agenta).
     * @param target       Pole docelowe (obiekt obserwowany).
     * @param localGridMap Mapa lokalnego otoczenia ułatwiająca szybkie pobieranie współrzędnych.
     * @return true, jeśli linia widzenia nie jest zablokowana przez ścianę; false w przeciwnym razie.
     */
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

    /**
     * Postarza agenta o jeden krok czasowy (tick).
     */
    public void ageUp(){
        age++;
    }

    /**
     * Zabija agenta, zmieniając flagę życiową oraz zerując punkty zdrowia.
     */
    public void die() {
        this.isAlive = false;
        this.healthLevel = 0;
    }

    /**
     * Zmienia poziom zdrowia agenta. W przypadku obrażeń u ocalałych uwzględnia redukcję
     * wynikającą z założonego pancerza/ubrania. Przy leczeniu istnieje szansa na usunięcie rany.
     *
     * @param amount Wartość zmiany (dodatnia dla leczenia, ujemna dla obrażeń).
     */
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
            if (RNG.nextFloat(1)> SimulationParameters.getInstance().getHealChance()) healWound();
        }
        if (this.healthLevel <= 0) {
            die();
        }
        if(healthLevel>maxHealthLevel) healthLevel = maxHealthLevel;
    }

    /**
     * Oblicza aktualną wartość pola widzenia (FOV).
     * Wpływ na pole widzenia ma aktualna pora dnia oraz ewentualne rany głowy.
     *
     * @return Aktualny promień pola widzenia (wartość nie mniejsza niż 0).
     */
    public int calculateFOV(){
        double visibilityMultiplier = 1.0; // Domyślna widoczność, jeśli nie ma środowiska

        if (SimulationApp.getEnvironment() != null) {
            visibilityMultiplier = TimeOfDay.getVisibilityLevel(SimulationApp.getEnvironment().getActualTick());
        }

        int currentFOV = (int) (baseFOV * visibilityMultiplier);

        for(Wound wound : wounds){
            if(wound instanceof HeadWound){
                currentFOV -= ((HeadWound) wound).getReduction();
            }
        }
        return Math.max(0, currentFOV);
    }

    /**
     * Oblicza aktualną siłę bojową agenta.
     * Uwzględnia rany rąk, stan głodu ocalałego oraz bonusy i zużycie posiadanej broni.
     *
     * @return Aktualna siła agenta (wartość nie mniejsza niż 1).
     */
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

    /**
     * Oblicza aktualną prędkość poruszania się agenta z uwzględnieniem ran nóg.
     *
     * @return Aktualny zasięg ruchu (wartość nie mniejsza niż 0).
     */
    public int calculateSpeed(){
        int currentSpeed = baseSpeed;
        for(Wound wound : wounds){
            if(wound instanceof LegWound){
                currentSpeed -= ((LegWound) wound).getReduction();
            }
        }
        return Math.max(0, currentSpeed);
    }

    /** @return true, jeśli agent żyje; false w przeciwnym wypadku. */
    public boolean isItAlive(){return isAlive;}

    /** @return Dwuelementowa tablica int[] z aktualną pozycją [x, y]. */
    public int[] getPosition(){ return new int[]{x, y}; }

    /** @return Aktualna liczba punktów zdrowia. */
    public int getHealth(){return healthLevel;}

    /**
     * Usuwa (leczy) losową ranę z listy ran agenta, jeśli jakaś istnieje.
     */
    public void healWound() {
        if (wounds.isEmpty()) return;
        wounds.remove(RNG.nextInt(wounds.size()));
    }

    /**
     * Dodaje agentowi losową ranę (głowy, ręki lub nogi).
     */
    public void reviveWound(){
        switch (RNG.nextInt(3)){
            case(0) -> wounds.add(new HeadWound());
            case(1) -> wounds.add(new ArmWound());
            case(2) -> wounds.add(new LegWound());
        }
    }

    /** @return Aktualny wiek agenta wyrażony w tura/tickach. */
    public int getAge(){return age;}
}