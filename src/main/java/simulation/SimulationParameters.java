package simulation;

import java.util.HashMap;
import java.util.Map;

/**
 * Globalny rejestr konfiguracji i parametrów sterujących przebiegiem symulacji.
 * Klasa implementuje wzorzec projektowy <b>Singleton</b>, zapewniając scentralizowany
 * i spójny dostęp do danych konfiguracyjnych z dowolnego miejsca w systemie.
 * <p>
 * Klasa obsługuje system gotowych profili trudności (Trudny, Łatwy, Zbalansowany),
 * które dynamicznie rekonfigurują liczebność populacji, gęstość zasobów,
 * prawdopodobieństwa mechanik losowych oraz wagi behawioralne dla algorytmów decyzyjnych AI.
 * </p>
 */
public class SimulationParameters {

    /** Jedyna instancja klasy w pamięci aplikacji (wzorzec Singleton). */
    private static SimulationParameters instance;

    /** Identyfikator aktualnie wybranego profilu ustawień (domyślnie profil 1). */
    private static int selectedProfile = 1;

    /** Początkowa liczba zdrowych ocalałych (Survivor) na mapie. */
    private int startingSurvivorAmount;

    /** Początkowa liczba zainfekowanych (Infected) na mapie. */
    private int startingInfectedAmount;

    /** * Mapa wag decyzyjnych dla algorytmów przemieszczania się agentów.
     * Klucze identyfikują bodźce środowiskowe, a wartości określają ich priorytet (dodatni lub ujemny).
     */
    private Map<String, Integer> moveWeights;

    /** * Tablica szans startowych:
     * Indeks 0: Szansa ocalałego na posiadanie broni (% [0-100]).
     * Indeks 1: Szansa zakażonego na posiadanie aktywnej rany (% [0-100]).
     */
    private int[] startingEqAndWoundChances = new int[]{10, 15};

    /** Ziarno (seed) zapewniające deterministyczne i powtarzalne generowanie liczb losowych. */
    private int simulationSeed = 128;

    /** Łączna liczba sztuk broni palnej/białej rozmieszczanej na planszy. */
    private int weaponCount;

    /** Łączna liczba zestawów odzieży ochronnej rozmieszczanej na planszy. */
    private int clothesCount;

    /** Całkowita liczba naturalnych punktów odnawialnych zasobów (jedzenie/leki). */
    private int resourceCount;

    /** Docelowa liczba stref bezpieczeństwa wznoszonych na mapie. */
    private int safeZoneCount;

    /** Wewnętrzny rozmiar boku kwadratowej strefy bezpieczeństwa (rozpiętość użytkowa bez muru obronnego). */
    private int safeZoneSize = 5;

    /** Procentowy próg wyburzenia ścian schronienia ([0.0, 1.0]), kwalifikujący strefę do bezpowrotnego upadku. */
    private float destructionThreshold = 0.5f;

    /** Prawdopodobieństwo ([0.0, 1.0]) pomyślnego zasklepienia rany agenta w bezpiecznej strefie w ciągu ticku. */
    private float healChance;

    /** Prawdopodobieństwo ([0.0, 1.0]) infekcji ocalałego przez zakażonego w wyniku przegranej potyczki. */
    private float infectionChance;

    /** Prawdopodobieństwo ([0.0, 1.0]) otwarcia głębokiej rany u agenta po zakończeniu starcia fizycznego. */
    private float chanceForWoundAfterBattle;

    /** * Statystyki bazowe ocalałego.
     * Indeks 0: Punkty zdrowia (Health), Indeks 1: Siła (Strength),
     * Indeks 2: Pole widzenia (FOV), Indeks 3: Prędkość ruchu (Speed).
     */
    private int[] survivorStats = new int[]{100, 20, 5, 3};

    /** * Statystyki bazowe zakażonego.
     * Indeks 0: Punkty zdrowia (Health), Indeks 1: Siła (Strength),
     * Indeks 2: Pole widzenia (FOV), Indeks 3: Prędkość ruchu (Speed).
     */
    private int[] infectedStats = new int[]{70, 7, 4, 5};

    /** * Parametry rozkładu prawdopodobieństwa anomalii pogodowych.
     * Indeks 0: Bazowa szansa na wystąpienie jakiegokolwiek zdarzenia.
     * Indeks 1: Waga losowania dla burzy.
     * Indeks 2: Waga losowania dla mgły.
     * Indeks 3: Waga losowania dla trzęsienia ziemi.
     */
    private double[] eventChances;

    /** Dwu-elementowa tablica określająca granice czasu trwania anomalii: [minimalna_liczba_ticków, maksymalna_liczba_ticków]. */
    private final int[] eventDuration = new int[]{1, 5};

    /** Prawdopodobieństwo jednostkowe ([0.0, 1.0]) zrujnowania konkretnej ściany w trakcie trzęsienia ziemi. */
    private final double earthquakeWallDestroyChance = 0.05;

    /** Zakres redukcji widoczności w trakcie wystąpienia mgły: [minimalne_zamglenie, maksymalne_zamglenie]. */
    private final double[] fogIntensity = new double[]{0.2, 0.6};

    /** Konfiguracja rytmu dobowego symulacji: [czas_trwania_dnia_w_tickach, czas_trwania_nocy_w_tickach]. */
    private final int[] dayNightCycle = new int[]{15, 7};

    /**
     * Prywatny konstruktor inicjalizujący mapę struktur decyzyjnych agentów.
     * Zabezpiecza klasę przed niekontrolowanym tworzeniem instancji spoza mechanizmu {@link #getInstance()}.
     */
    private SimulationParameters() {
        moveWeights = new HashMap<>();
        loadDefaultWeights();
    }

    /**
     * Definiuje oraz ładuje wybrany profil nastawów symulacji.
     * Jeśli instancja klasy została już powołana do życia, parametry zostaną natychmiast nadpisane.
     *
     * @param profileNumber Numer profilu do aktywacji (dostępne: 1 - Trudny, 2 - Łatwy, 3 - Zbalansowany).
     * @throws IllegalArgumentException Jeśli przekazany numer wykracza poza dozwolony zakres 1-3.
     */
    public static void setProfile(int profileNumber) {
        if (profileNumber < 1 || profileNumber > 3) {
            throw new IllegalArgumentException("Dostępne profile to: 1, 2 lub 3");
        }
        selectedProfile = profileNumber;
        if (instance != null) {
            instance.loadProfile(selectedProfile);
        }
    }

    /**
     * Nadaje nową wartość ziarna losowości dla silnika symulacji.
     * Pobierana wartość jest konwertowana na liczbę absolutną (dodatnią).
     *
     * @param seedNumber Wartość liczbowa seeda.
     */
    public void setSeed(int seedNumber){
        simulationSeed = Math.abs(seedNumber);
    }

    /**
     * Zwraca unikalną, globalną instancję obiektu parametrów.
     * Wykorzystuje wzorzec leniwej inicjalizacji (Lazy Initialization) przy pierwszym odpytaniu.
     *
     * @return Scentralizowany obiekt {@link SimulationParameters}.
     */
    public static SimulationParameters getInstance(){
        if (instance == null){
            instance = new SimulationParameters();
            instance.loadProfile(selectedProfile);
        }
        return instance;
    }

    /**
     * Dokonuje wewnętrznego wstrzyknięcia wartości liczbowych przypisanych do konkretnego profilu.
     * <ul>
     * <li><b>Profil 1 (Trudny):</b> Drastyczna przewaga zakażonych, szczątkowe zasoby, rzadkie strefy schronień.</li>
     * <li><b>Profil 2 (Łatwy):</b> Duża populacja startowa ludzi, obfitość uzbrojenia, gęsto usiane Safe-Zony.</li>
     * <li><b>Profil 3 (Zbalansowany):</b> Wypośrodkowany rozkład szans przetrwania i symetrii zagrożeń.</li>
     * </ul>
     *
     * @param profileNumber Indeks profilu przekazany z kontrolera.
     */
    private void loadProfile(int profileNumber) {
        switch (profileNumber) {
            case 1:
                this.startingSurvivorAmount = 25;
                this.startingInfectedAmount = 500;
                this.weaponCount = 30;
                this.clothesCount = 30;
                this.resourceCount = 100;
                this.safeZoneCount = 1;
                this.healChance = 0.25f;
                this.infectionChance = 0.10f;
                this.chanceForWoundAfterBattle = 0.20f;
                this.eventChances = new double[]{0.25, 10, 20, 2};
                break;

            case 2:
                this.startingSurvivorAmount = 40;
                this.startingInfectedAmount = 200;
                this.weaponCount = 200;
                this.clothesCount = 200;
                this.resourceCount = 400;
                this.safeZoneCount = 3;
                this.healChance = 0.50f;
                this.infectionChance = 0.15f;
                this.chanceForWoundAfterBattle = 0.20f;
                this.eventChances = new double[]{0.30, 10, 20, 2};
                break;

            case 3:
                this.startingSurvivorAmount = 25;
                this.startingInfectedAmount = 450;
                this.weaponCount = 100;
                this.clothesCount = 100;
                this.resourceCount = 180;
                this.safeZoneCount = 2;
                this.healChance = 0.35f;
                this.infectionChance = 0.15f;
                this.chanceForWoundAfterBattle = 0.25f;
                this.eventChances = new double[]{0.60, 20, 40, 4};
                break;
        }
    }

    /**
     * Konfiguruje domyślne stałe matematyczne (wagi heurystyczne) wektora ruchu sieci neuronowej/AI agentów.
     * Wagi dodatnie stymulują dążenie do celu (atrakcyjność), wagi ujemne wywołują wektor ucieczki (repulsja).
     */
    private void loadDefaultWeights() {
        // Wagi behawioralne hordy zakażonych
        moveWeights.put("infectedCurrentSeenSurvivor", 200);
        moveWeights.put("infectedMemory", 25);
        moveWeights.put("infectedCloseInfected", 50);

        // Wagi behawioralne ocalałych ludzi
        moveWeights.put("survivorInfected", -100);
        moveWeights.put("survivorSafeZone", 80);
        moveWeights.put("survivorResource", 50);
        moveWeights.put("survivorSurvivor", -125);
        moveWeights.put("survivorEquipment", 150);
    }

    /**
     * Zwraca liczebność startową obu frakcji.
     *
     * @return Dwuelementowa tablica int: {@code [liczba_ocalałych, liczba_zakażonych]}.
     */
    public int[] getAgentsAmount() { return new int[]{startingSurvivorAmount, startingInfectedAmount}; }

    /**
     * Zwraca szanse procentowe na startowe wyposażenie i uszkodzenia ciała.
     *
     * @return Dwuelementowa tablica int: {@code [szansa_na_broń, szansa_na_ranę]}.
     */
    public int[] getEqAndWoundChances() { return startingEqAndWoundChances; }

    /**
     * Zwraca skonfigurowane ziarno generatora losowego.
     *
     * @return Wartość ziarna (seed).
     */
    public int getSimulationSeed(){ return simulationSeed; }

    /**
     * Zwraca strukturę szans i wag losowania katastrof klimatycznych.
     *
     * @return Tablica wartości zmiennoprzecinkowych dla EventManagera.
     */
    public double[] getEventChances(){ return eventChances; }

    /**
     * Zwraca granice czasu trwania anomalii.
     *
     * @return Tablica int: {@code [min_ticki, max_ticki]}.
     */
    public int[] getEventDuration(){ return eventDuration; }

    /**
     * Zwraca szansę na zawalenie się struktury ściennej pod wpływem wstrząsu sejsmicznego.
     *
     * @return Szansa jako ułamek dziesiętny.
     */
    public double getEarthquakeWallDestroyChance(){ return earthquakeWallDestroyChance; }

    /**
     * Zwraca progi intensywności redukcji pola widzenia przez mgłę.
     *
     * @return Tablica double z przedziałami gęstości mgły.
     */
    public double[] getFogIntensity(){ return fogIntensity; }

    /**
     * Zwraca długość interwałów cyklu dobowego.
     *
     * @return Tablica int: {@code [długość_dnia, długość_nocy]}.
     */
    public int[] getDayNightCycle(){ return dayNightCycle; }

    /**
     * Zwraca kompletną mapę wag sterujących wektorami ruchu sztucznej inteligencji.
     *
     * @return Mapa par {@code String -> Integer} opisująca priorytety nawigacji.
     */
    public Map<String, Integer> getMoveWeights() { return moveWeights; }

    /**
     * Zwraca docelową liczbę broni palnej/białej na mapie.
     *
     * @return Liczba sztuk broni.
     */
    public int getWeaponCount() { return weaponCount; }

    /**
     * Zwraca docelową liczbę ubrań ochronnych na mapie.
     *
     * @return Liczba sztuk odzieży.
     */
    public int getClothesCount() { return clothesCount; }

    /**
     * Zwraca docelową liczbę odnawialnych punktów zasobów konsumpcyjnych.
     *
     * @return Liczba punktów zasobów.
     */
    public int getResourceCount() { return resourceCount; }

    /**
     * Zwraca genotypowe statystyki startowe dla klasy ocalałego (Survivor).
     *
     * @return Tablica int odzwierciedlająca profil: {@code [HP, siła, FOV, prędkość]}.
     */
    public int[] getSurvivorStats(){ return survivorStats; }

    /**
     * Zwraca genotypowe statystyki startowe dla klasy zakażonego (Infected).
     *
     * @return Tablica int odzwierciedlająca profil: {@code [HP, siła, FOV, prędkość]}.
     */
    public int[] getInfectedStats(){ return infectedStats; }

    /**
     * Zwraca planowaną liczbę bezpiecznych stref do rozstawienia.
     *
     * @return Liczba stref.
     */
    public int getSafeZoneCount(){ return safeZoneCount; }

    /**
     * Zwraca gabaryt szerokości wewnętrznej bezpiecznego obozu.
     *
     * @return Długość boku w kafelkach.
     */
    public int getSafeZoneSize(){ return safeZoneSize; }

    /**
     * Zwraca współczynnik destrukcji murów powodujący dezaktywację schronienia.
     *
     * @return Próg zniszczeń w formacie float.
     */
    public float getDestructionThreshold() { return destructionThreshold; }

    /**
     * Zwraca bazowe prawdopodobieństwo udanej kuracji medycznej w strefie.
     *
     * @return Szansa uleczenia ran.
     */
    public float getHealChance(){ return healChance; }

    /**
     * Zwraca współczynnik wirulencji patogenu (szansę na zakażenie ocalałego).
     *
     * @return Szansa na infekcję.
     */
    public float getInfectionChance(){ return infectionChance; }

    /**
     * Zwraca szansę na powstanie krwawiącej rany u agenta po potyczce bojowej.
     *
     * @return Szansa na zranienie.
     */
    public float getChanceForWoundAfterBattle(){ return chanceForWoundAfterBattle; }
}