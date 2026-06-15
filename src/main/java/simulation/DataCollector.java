package simulation;

import agent.Agent;
import agent.Infected;
import agent.Survivor;

/**
 * Klasa odpowiedzialna za gromadzenie, aktualizację oraz przetwarzanie danych statystycznych symulacji.
 * Służy do śledzenia bieżących wskaźników populacji (liczba agentów, średni wiek, średnie zdrowie),
 * rejestrowania częstotliwości interakcji oraz wyznaczania momentu krytycznego (wyginięcia ocalałych).
 */
public class DataCollector {
    /** Aktualna liczba żyjących ocalałych (Survivors). */
    private int survivorAmount;

    /** Aktualna liczba żyjących zainfekowanych (Infected). */
    private int infectedAmount;

    /** Licznik interakcji typu ocalały-zainfekowany. */
    private int survivorInfectedInteractions = 0;

    /** Licznik interakcji typu ocalały-ocalały. */
    private int survivorSurvivorInteractions = 0;

    /** Licznik ran wyleczonych przez agentów w bezpiecznych strefach (Safe Zones). */
    private int healedWoundInSafeZones = 0;

    /** Średni poziom zdrowia wszystkich żyjących w symulacji agentów. */
    private float meanHealth;

    /** Średni wiek (w tickach/turach) wszystkich żyjących w symulacji agentów. */
    private float meanAge;

    /** * Numer tury (ticka), w której wyginęli wszyscy ocalali.
     * Wartość {@code -1} oznacza, że przynajmniej jeden ocalały wciąż żyje.
     */
    private int timeToSurvivorsExtinction = -1;

    /**
     * Domyślny konstruktor inicjalizujący obiekt zbierający dane.
     */
    public DataCollector() {
    }

    /**
     * Przeszukuje listę agentów w podanym środowisku symulacji i aktualizuje
     * wszystkie globalne statystyki (liczebność, średnie zdrowie, średni wiek).
     * Wyznacza również moment wyginięcia ocalałych, jeśli nastąpił on w bieżącym kroku.
     *
     * @param s Środowisko symulacji {@link SimulationEnvironment}, z którego pobierana jest aktualna lista agentów.
     */
    public void updateData(SimulationEnvironment s) {
        int survivors = 0;
        int infected = 0;
        double totalHealth = 0;
        double totalAge = 0;
        int totalAgents = 0;

        // Zliczanie agentów i sumowanie statystyk
        for (Agent a : s.getAgentList()) {
            if (a.isItAlive()) {
                if (a instanceof Survivor) {
                    survivors++;
                } else if (a instanceof Infected) {
                    infected++;
                }
                totalHealth += a.getHealth();
                totalAge += a.getAge();
                totalAgents++;
            }
        }

        this.survivorAmount = survivors;
        this.infectedAmount = infected;
        this.meanHealth = totalAgents > 0 ? (float) (totalHealth / totalAgents) : 0f;
        this.meanAge = totalAgents > 0 ? (float) (totalAge / totalAgents) : 0f;

        // Zapisz tick wyginięcia jeśli właśnie wyginęli
        if (survivors == 0 && this.timeToSurvivorsExtinction == -1 && s.getActualTick() > 0) {
            this.timeToSurvivorsExtinction = s.getActualTick();
        }
    }

    /**
     * Zwiększa o 1 licznik odnotowanych interakcji pomiędzy ocalałymi a zainfekowanymi.
     */
    public void incSurvivorInfectedInteractions() { this.survivorInfectedInteractions++; }

    /**
     * Zwiększa o 1 licznik odnotowanych interakcji pomiędzy dwoma ocalałymi.
     */
    public void incSurvivorSurvivorInteractions() { this.survivorSurvivorInteractions++; }

    /**
     * Zwiększa o 1 licznik ran, które zostały pomyślnie wyleczone w obrębie bezpiecznych stref.
     */
    public void incHealedWoundInSafeZones() { this.healedWoundInSafeZones++; }

    /**
     * @return Aktualna liczba żyjących ocalałych.
     */
    public int getSurvivorAmount() { return survivorAmount; }

    /**
     * @return Aktualna liczba żyjących zainfekowanych.
     */
    public int getInfectedAmount() { return infectedAmount; }

    /**
     * @return Całkowita liczba zarejestrowanych starć/interakcji ocalały-zainfekowany.
     */
    public int getSurvivorInfectedInteractions() { return survivorInfectedInteractions; }

    /**
     * @return Całkowita liczba zarejestrowanych spotkań/interakcji ocalały-ocalały.
     */
    public int getSurvivorSurvivorInteractions() { return survivorSurvivorInteractions; }

    /**
     * @return Całkowita liczba ran wyleczonych w strefach bezpiecznych.
     */
    public int getHealedWoundInSafeZones() { return healedWoundInSafeZones; }

    /**
     * @return Średnia wartość zdrowia wyliczona dla aktualnie żyjącej populacji.
     */
    public float getMeanHealth() { return meanHealth; }

    /**
     * @return Średni wiek (liczba tury) osiągnięty przez aktualnie żyjących agentów.
     */
    public float getMeanAge() { return meanAge; }

    /**
     * @return Numer tury wyginięcia ocalałych lub {@code -1}, jeśli ocalali wciąż trwają w symulacji.
     */
    public int getTimeToSurvivorsExtinction() { return timeToSurvivorsExtinction; }
}