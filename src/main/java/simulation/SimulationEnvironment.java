package simulation;

import agent.*;
import equipment.Clothes;
import equipment.Equipment;
import equipment.EquipmentFactory;
import equipment.EquipmentSpawnStrategy;
import event.EventManager;
import event.TimeOfDay;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Główny silnik i środowisko uruchomieniowe symulacji.
 * Klasa odpowiada za zarządzanie pełnym cyklem życia świata symulacji: od proceduralnego
 * generowania mapy (automaty komórkowe), przez inicjalizację populacji, aż po cykliczne
 * wykonywanie kroków tury (ticków).
 * <p>
 * Środowisko koordynuje ruch agentów, walki (zarówno między ocalałymi i zakażonymi, jak
 * i wewnętrzne starcia ocalałych), odnawianie zasobów, anomalie pogodowe oraz destrukcję
 * stref bezpieczeństwa. Plansza cechuje się topologią toroidalną (zawijanie krawędzi bocznych oraz górnych/dolnych).
 * </p>
 */
public class SimulationEnvironment {

    /** Ewentualna instancja referencyjna środowiska. */
    private static SimulationEnvironment instance;

    /** Dwuwymiarowa siatka pól stanowiąca mapę świata symulacji. */
    private Space[][] board;

    /** Globalna lista wszystkich żywych agentów zarejestrowanych w symulacji. */
    private List<Agent> agentList = new ArrayList<>();

    /** Zbiór pomocniczy zapobiegający wielokrotnemu ruchowi tego samego agenta w obrębie jednego ticku. */
    private Set<Agent> usedAgentList = new HashSet<>();

    /** Rejestr wszystkich zasobów środowiskowych (jedzenie, apteczki) rozlokowanych na mapie. */
    private List<EnvironmentalResource> resources = new ArrayList<>();

    /** Licznik czasu symulacji wyrażony w jednostkach tick (turach). */
    private int actualTick = 0;

    /** Referencja do konfiguracji parametrów globalnych symulacji. */
    private final SimulationParameters parameters = SimulationParameters.getInstance();

    /** Obiekt analityczny agregujący i wyliczający bieżące statystyki populacyjne. */
    private DataCollector data = new DataCollector();

    /** Lista aktualnie aktywnych stref bezpieczeństwa (schronień). */
    private List<SafeZone> zones = new ArrayList<>();

    /** Bufor komunikatów tekstowych logujących kluczowe wydarzenia z bieżącej tury. */
    private final List<String> turnLogs = new ArrayList<>();

    /** Komponent odpowiedzialny za graficzne rysowanie stanu środowiska. */
    private Render render = new Render();

    /** Przechowuje informację o bieżącej porze dnia ("Dzień" lub "Noc"). */
    private String timeOfDay;

    /** Strategia polimorficzna odpowiedzialna za algorytm rozmieszczania przedmiotów. */
    private final EquipmentSpawnStrategy equipmentSpawnStrategy;

    /**
     * Konstruktor środowiska symulacji. Przeprowadza sekwencyjną, kompletną procedurę setupu świata.
     * Wykonuje kolejno:
     * <ol>
     * <li>Inicjalizację siatki i wygenerowanie korytarzy/ścian (Cellular Automata)</li>
     * <li>Wygenerowanie ufortyfikowanych stref bezpieczeństwa</li>
     * <li>Rozmieszczenie populacji ocalałych i zainfekowanych na bezpiecznych pozycjach</li>
     * <li>Rozrzucenie przedmiotów na ziemi (broń, odzież)</li>
     * <li>Wygenerowanie punktów zbieractwa zasobów</li>
     * <li>Wstępne przeliczenie danych statystycznych</li>
     * </ol>
     *
     * @param width  Szerokość planszy wyrażona w liczbie kafelków.
     * @param height Wysokość planszy wyrażona w liczbie kafelków.
     */
    public SimulationEnvironment(int width, int height){
        this.equipmentSpawnStrategy = this::spawnEquipmentRandomly;
        createBoard(width, height);
        createSafeZones(width, height);
        createAgents(parameters.getAgentsAmount()[0], parameters.getAgentsAmount()[1]);
        spawnEquipmentOnBoard();
        createResources(parameters.getResourceCount());
        data.updateData(this);
    }

    /**
     * Główna metoda krokowa (pipeline tury). Wywoływana cyklicznie przez zegar aplikacji.
     * Spina w spójną całość logikę fazową symulacji:
     * <ol>
     * <li>Weryfikacja i aplikacja losowych zdarzeń losowych (anomalii środowiskowych)</li>
     * <li>Wykonanie fazy ruchu przez wszystkich uprawnionych agentów</li>
     * <li>Rozstrzygnięcie interakcji przestrzennych (walki wręcz, zbieractwo, transformacje)</li>
     * <li>Czyszczenie pamięci z agentów permanentnie martwych</li>
     * <li>Aktualizacja zegara świata, starzenie populacji i regeneracja zasobów</li>
     * <li>Przeliczenie statystyk i wywołanie renderowania grafiki</li>
     * </ol>
     *
     * @param gc       Kontekst graficzny JavaFX wykorzystywany do rysowania.
     * @param tileSize Skala wyświetlania pojedynczego kafelka.
     * @return {@code true} jeżeli w symulacji wciąż żyje przynajmniej jeden ocalały;
     * {@code false} w przypadku całkowitej ekstynkcji ocalałych (koniec symulacji).
     */
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

    /**
     * Zwraca listę wszystkich agentów obecnie zarejestrowanych w środowisku symulacji.
     *
     * @return Lista obiektów typu {@link Agent}.
     */
    public List<Agent> getAgentList() { return agentList; }

    /**
     * Zwraca aktualny wiek świata mierzony w tickach.
     *
     * @return Numer bieżącej tury.
     */
    public int getActualTick() { return this.actualTick; }

    /**
     * Aktualizuje parametry czasowe świata. Inkrementuje licznik ticków, inkrementuje wiek
     * żyjących agentów, obsługuje pętlę regeneracji (respawnu) zużytych surowców naturalnych
     * oraz przelicza natężenie światła słonecznego determinując porę dnia.
     */
    private void updateTimeOfDay(){
        actualTick++;
        // Odnawianie zasobów (respawn po czasie)
        for(EnvironmentalResource resource : resources){
            if(resource.wasUsed()) {
                resource.updateTime();
                resource.Respawn();
            }
        }
        // Zwiększenie wieku wszystkich agentów
        for(Agent a : agentList){
            a.ageUp();
        }

        // Określenie pory dnia na podstawie widoczności (0.57 to próg dzień/noc)
        if(TimeOfDay.getVisibilityLevel(actualTick) < 0.57) timeOfDay = "Noc";
        else timeOfDay = "Dzień";
    }

    /** Procentowa szansa na to, że pole stanie się ścianą w kroku zerowym automatu komórkowego. */
    private static final int INITIAL_WALL_CHANCE = 35;

    /** Liczba powtórzeń filtracji wygładzającej sąsiedztwo w automacie komórkowym. */
    private static final int SMOOTHING_STEPS = 6;

    /** Minimalna liczba ścian w sąsiedztwie Moore'a, by puste pole przekształciło się w ścianę (efekt narodzin). */
    private static final int BIRTH_THRESHOLD = 5;

    /** Minimalna liczba barier wokół istniejącej ściany, by nie uległa ona samoistnemu zburzeniu (efekt przeżycia). */
    private static final int DEATH_THRESHOLD = 3;

    /**
     * Generuje strukturę planszy z wykorzystaniem algorytmu automatów komórkowych (Cellular Automata).
     * Tworzy naturalnie wyglądające, jaskiniowe lub zrujnowane układy barier architektonicznych.
     * Dodatkowo metoda spina ze sobą referencje kafelków sąsiadujących, implementując
     * pełne toroidalne zawijanie współrzędnych (góra łączy się z dołem, lewo z prawem)
     * w celu umożliwienia poprawnego działania algorytmów przeszukiwania grafu (np. BFS agentów).
     *
     * @param width  Docelowa szerokość generowanej planszy.
     * @param height Docelowa wysokość generowanej planszy.
     */
    private void createBoard(int width, int height){
        board = new Space[height][width];

        // Inicjalizacja losowych ścian
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                board[y][x] = new Space(x, y);
                if(RNG.nextInt(100) < INITIAL_WALL_CHANCE){
                    board[y][x].createWall();
                }
            }
        }

        // Wygładzanie mapy (iteracyjne)
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
            // Zastosowanie nowej mapy ścian
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    if(nextWalls[y][x]) board[y][x].createWall();
                    else board[y][x].destroyWallSilent();
                }
            }
        }

        // Łączenie sąsiednich pól (dla nawigacji w BFS) z uwzględnieniem zawijania brzegów planszy
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                // Połączenia poziome (lewo-prawo z zawijaniem)
                if(x > 0){
                    board[y][x].joinLeft(board[y][x-1]);
                    board[y][x-1].joinRight(board[y][x]);
                }
                if(x == width - 1){
                    board[y][0].joinLeft(board[y][x]);
                    board[y][x].joinRight(board[y][0]);
                }
            }
            // Połączenia pionowe (góra-dół z zawijaniem)
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

    /**
     * Zlicza ściany sąsiadujące z podanym punktem w macierzy w oparciu o sąsiedztwo Moore'a (8 kierunków).
     * Uwzględnia toroidalną strukturę świata przy wychodzeniu poza indeksy tablicy.
     *
     * @param x      Współrzędna X sprawdzanego kafelka.
     * @param y      Współrzędna Y sprawdzanego kafelka.
     * @param width  Maksymalna szerokość planszy.
     * @param height Maksymalna wysokość planszy.
     * @return Liczba ścian otaczających punkt (wartość od 0 do 8).
     */
    private int countWallNeighbours(int x, int y, int width, int height){
        int count = 0;
        for(int dy = -1; dy <= 1; dy++){
            for(int dx = -1; dx <= 1; dx++){
                if(dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                // krawędź planszy traktujemy jako ścianę (zawijanie toroidalne)
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

    /**
     * Generuje i rozmieszcza kwadratowe strefy bezpieczeństwa (Safe Zones) na planszy.
     * Metoda dba o czyszczenie naturalnych przeszkód wokół schronienia (tworzy pas przejściowy),
     * wznosi zewnętrzne mury obronne strefy oraz wybija w nich cztery symetryczne bramy
     * wejściowe (północ, południe, wschód, zachód). Całość zabezpieczona jest algorytmem
     * anty-nakładaniowym chroniącym przed generowaniem stref zbyt blisko siebie.
     *
     * @param width  Szerokość operacyjna planszy.
     * @param height Wysokość operacyjna planszy.
     */
    private void createSafeZones(int width, int height){
        int safeZoneCount = parameters.getSafeZoneCount();
        int safeZoneSize = parameters.getSafeZoneSize();
        float healChance = parameters.getHealChance();
        float destructionThreshold = parameters.getDestructionThreshold();

        int attempts = 0;
        int created = 0;

        // Próba utworzenia zadanej liczby stref (max 100 prób)
        while(created < safeZoneCount && attempts < 100){
            attempts++;

            int margin = safeZoneSize + 4; // zwiększony margines z powodu czyszczenia 2 pól
            int startX = RNG.nextInt(width - margin * 2) + margin;
            int startY = RNG.nextInt(height - margin * 2) + margin;

            // Sprawdź czy nowa strefa nie nachodzi na istniejącą
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

            // Czyszczenie ścian wokół strefy (bufor dla przejścia)
            for(int dy = -3; dy <= safeZoneSize + 2; dy++){
                for(int dx = -3; dx <= safeZoneSize + 2; dx++){
                    int nx = startX + dx;
                    int ny = startY + dy;

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

                    // Usuń ścianę jeśli nie należy do strefy
                    if(!isPartOfSafeZone && board[ny][nx].isItWall()){
                        board[ny][nx].destroyWallSilent();
                    }
                }
            }

            int centerX = startX + safeZoneSize / 2;
            int centerY = startY + safeZoneSize / 2;

            // Tworzenie 4 wejść do strefy (N,S,W,E)
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

    /**
     * Metoda walidacyjna sprawdzająca geometryczne nakładanie się nowo projektowanej
     * strefy bezpieczeństwa z obiektami stref już osadzonych na mapie (wraz z uwzględnieniem marginesu).
     *
     * @param startX Współrzędna X punktu początkowego nowej strefy.
     * @param startY Współrzędna Y punktu początkowego nowej strefy.
     * @return {@code true} jeśli wykryto kolizję przestrzenną; {@code false} jeśli teren jest wolny.
     */
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

    /**
     * Przetwarza wewnętrzny stan stref bezpieczeństwa. Wykonuje operacje regeneracji zdrowia
     * u ocalałych przebywających w bezpiecznych strefach (warunkiem jest brak stanu głodu)
     * oraz identyfikuje i usuwa z rejestrów strefy, których czas życia upłynął bądź bariery obronne upadły.
     */
    private void healSurvivorsInSafeZones(){
        List<SafeZone> expiredZones = new ArrayList<>();

        for(SafeZone zone : zones){
            // Sprawdź czy strefa wygasła lub została zniszczona
            if(zone.updateAndCheckExpiry()){
                expiredZones.add(zone);
                turnLogs.add("Strefa bezpieczeństwa została zniszczona!");
                continue;
            }
            // Leczenie ocalałych w strefie
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

    /**
     * Rozmieszcza losowo określoną liczbę punktów zasobów środowiskowych (żywność/medykamenty) na mapie.
     * Lokacja jest poprawna wyłącznie wtedy, gdy kafelek nie jest ścianą, nie leży wewnątrz
     * żadnej strefy bezpieczeństwa oraz sam nie zawiera już innego surowca.
     *
     * @param resourceNumber Łączna liczba punktów zasobów do wygenerowania.
     */
    private void createResources(int resourceNumber){
        int width = board[0].length;
        int height = board.length;
        int i = 0;
        while(i < resourceNumber){
            int x = RNG.nextInt(width);
            int y = RNG.nextInt(height);
            if (!board[y][x].isItWall()&& !board[y][x].isInSafeZone() && !board[y][x].containsResource()){
                EnvironmentalResource resource = new EnvironmentalResource();
                board[y][x].addResource(resource);
                resources.add(resource);
                i++;
            }
        }
    }

    /**
     * Inicjalizuje populację początkową symulacji, wprowadzając zdefiniowaną liczbę ocalałych
     * oraz zakażonych. Agenci są losowo rozrzucani po wolnej przestrzeni (poza murami i safe-zone).
     * <p>
     * Podczas spawnu system losowo wyposaża część ocalałych w broń startową oraz generuje rany
     * u części populacji zakażonych (w oparciu o szanse procentowe z konfiguracji).
     * </p>
     *
     * @param survivorNumber Liczba ocalałych (Survivor) do utworzenia.
     * @param infectedNumber Liczba zainfekowanych (Infected) do utworzenia.
     */
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

    /**
     * Wykonuje fazę ruchu dla wszystkich żywych agentów. Przeszukuje planszę kafelek po kafelku,
     * pobiera agentów i wywołuje ich dedykowane algorytmy decyzyjne. Przemieszczenie na mapie
     * realizowane jest przez metodę {@link #displaceAgent(int[], int[], Agent)}.
     */
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
                            // Upewnij się że każdy agent poruszy się tylko raz w ticku
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

    /**
     * Dokonuje fizycznej translacji pozycji agenta w strukturze danych planszy (przypisuje
     * obiekt do nowej instancji pola {@link Space} i usuwa go z pozycji dotychczasowej).
     *
     * @param originalSpace Tablica dwuelementowa [Y, X] pozycji wyjściowej.
     * @param targetSpace   Tablica dwuelementowa [X, Y] pozycji docelowej.
     * @param a             Obiekt przemieszczanego agenta.
     */
    private void displaceAgent(int[] originalSpace, int[] targetSpace, Agent a){
        int originalY = originalSpace[0];
        int originalX = originalSpace[1];
        int targetX = targetSpace[0];
        int targetY = targetSpace[1];
        board[targetY][targetX].addAgent(a);
        board[originalY][originalX].deleteAgent(a);
    }

    /**
     * Krytyczna metoda rozstrzygająca konflikty przestrzenne wynikające z obecności wielu agentów
     * na jednym kafelku.
     * <p>
     * Logika przetwarza zdarzenia w następującej kolejności:
     * </p>
     * <ul>
     * <li><b>Walka Ocalały vs Ocalały (Anarchia):</b> Uruchamia się wyłącznie poza strefami bezpieczeństwa.
     * Agenci walczą na śmierć i życie o ekwipunek. Zwycięzca przejmuje ("kradnie") przedmioty pokonanego.</li>
     * <li><b>Walka Ocalały vs Zakażony:</b> Starcie frakcyjne. Zakażeni zadają obrażenia i posiadają
     * szansę na zainfekowanie ocalałego (rzut kostką, przed infekcją chronić może specjalny ubiór).
     * Sukces infekcji natychmiast mutuje ocalałego w nowego zakażonego (metoda {@link #transformSurvivor}).</li>
     * <li><b>Lootowanie/Zbieractwo:</b> Agenci, którzy przetrwali fazę starć bojowych, podnoszą z ziemi
     * porzucony ekwipunek bądź konsumują surowce naturalne obecne na polu (regenerując energię i zdrowie).</li>
     * </ul>
     */
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

                    // Podział agentów na ocalałych i zakażonych
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

                            // Usuwanie martwych
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
                                // Zakażony atakuje
                                survivor.changeHealthLevel(-Math.max(5, zakazonyWeight - (survivorWeight / 2)));
                                if (RNG.nextFloat() < parameters.getHealChance()) zakazony.changeHealthLevel(5);
                                if (RNG.nextFloat() < parameters.getChanceForWoundAfterBattle()) survivor.reviveWound();
                                // Próba infekcji
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
                                // Ocalały atakuje
                                zakazony.changeHealthLevel(-Math.max(5, survivorWeight - (zakazonyWeight / 2)));
                                if (RNG.nextFloat() < parameters.getChanceForWoundAfterBattle()) zakazony.reviveWound();
                            }

                            // Usuwanie martwych
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

                    // podnoszenie ekwipunku i zasobów (tylko pierwsi ocalali na polu)
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

    /**
     * Obsługuje proces biologicznej transformacji jednostki. Pobiera genotyp i cechy ocalałego,
     * wywołuje mutację generującą nowy obiekt typu {@link Infected}, usuwa instancję ocalałego
     * i natychmiast rejestruje zmutowanego potwora na kafelku mapy.
     *
     * @param o Obiekt ocalałego podlegający transformacji.
     * @param z Napastnik (zainfekowany), który zainicjował zakażenie.
     * @param x Pozycja X zdarzenia.
     * @param y Pozycja Y zdarzenia.
     */
    private void transformSurvivor(Survivor o, Infected z, int x, int y) {
        Infected newInfected = o.transformIntoInfected(z);
        turnLogs.add("Ocalały na pozycji [" + x + ", " + y + "] zmienił się w Zakażonego!");
        o.die();
        board[y][x].deleteAgent(o);
        agentList.add(newInfected);
        board[y][x].addAgent(newInfected);
    }

    /**
     * Wyrzuca z silnika symulacji obiekty agentów zakwalifikowanych jako martwi. Usuwa ich
     * referencje z poszczególnych kafelków planszy oraz z centralnego rejestru {@code agentList}.
     */
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

    /**
     * Deleguje odpytanie menedżera zdarzeń losowych w celu sprawdzenia prawdopodobieństwa
     * wystąpienia globalnej katastrofy (mgła, trzęsienie ziemi, burza) w bieżącym kroku czasu.
     */
    private void considerRandomEvent(){
        EventManager.runEventCheck(this.board);
    }

    /**
     * Aktywuje wdrożoną strategię rozmieszczania przedmiotów (ekwipunku) na mapie na bazie
     * stałych limitów ilościowych uzyskanych z konfiguracji parametrów.
     */
    private void spawnEquipmentOnBoard() {
        int weaponCount = parameters.getWeaponCount();
        int clothesCount = parameters.getClothesCount();
        equipmentSpawnStrategy.spawnEquipment(board, weaponCount, clothesCount);
    }

    /**
     * Pomocniczy algorytm losujący, rozrzucający przedmioty po kolekcji wolnych lokacji.
     * Pilnuje, by na jednym kafelku nie kumulowało się zbyt wiele niezależnych pakietów ekwipunku.
     *
     * @param freeSpaces Lista kafelków spełniających kryteria spawnu.
     * @param count      Docelowa liczba przedmiotów do wygenerowania.
     * @param type       Kategoria przedmiotu (broń lub odzież).
     */
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

    /**
     * Podstawowa strategia rozmieszczania ekwipunku (losowo na wolnych polach).
     * Skanuje całą mapę, odrzuca ściany oraz strefy bezpieczne, budując bazę lokalizacji
     * dla metody {@link #spawnItems(List, int, EquipmentFactory.EquipmentType)}.
     *
     * @param board        Struktura siatki świata.
     * @param weaponCount  Liczba broni do wrzucenia na mapę.
     * @param clothesCount Liczba sztuk odzieży do wrzucenia na mapę.
     */
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

    /**
     * Zwraca dwuwymiarową tablicę pól stanowiącą macierz planszy symulacji.
     *
     * @return Siatka kafelków {@link Space}[][].
     */
    public Space[][] getBoard() { return board; }
}