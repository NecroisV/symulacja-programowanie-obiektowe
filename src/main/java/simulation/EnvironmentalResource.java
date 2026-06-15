package simulation;

/**
 * Klasa reprezentująca zasób środowiskowy obecny na planszy symulacji (np. jedzenie, apteczka).
 * Zasoby mogą być zbierane przez agentów w celu odzyskania sił życiowych (energii oraz zdrowia).
 * Po zebraniu zasób staje się nieaktywny i po pewnym czasie przechodzi proces odradzania się (respawnu).
 */
public class EnvironmentalResource {
    /** Ilość energii odzyskiwanej przez agenta po zebraniu zasobu. */
    private int energyRecovery;

    /** Ilość punktów zdrowia odzyskiwanych przez agenta po zebraniu zasobu. */
    private int healthRecovery;

    /** Minimalny czas (liczba ticków), jaki musi upłynąć od użycia, aby zasób mógł się odrodzić. */
    private int minimalRespawnTime;

    /** Licznik czasu (w tickach) odmierzany od momentu zebrania zasobu. Wartość {@code -1} oznacza, że zasób jest gotowy do zebrania. */
    private int timeFromUse;

    /** Flaga określająca, czy zasób został już zebrany i jest aktualnie nieaktywny. */
    private boolean isUsed;

    /**
     * Konstruktor tworzący nowy zasób środowiskowy.
     * Automatycznie losuje parametry odzysku oraz czasu regeneracji w zdefiniowanych przedziałach:
     * <ul>
     * <li>Energia: 20-29 jednostek</li>
     * <li>Zdrowie: 15-24 jednostek</li>
     * <li>Minimalny czas respawnu: 20-29 ticków</li>
     * </ul>
     */
    public EnvironmentalResource(){
        energyRecovery = RNG.nextInt(20, 30);
        healthRecovery = RNG.nextInt(15, 25);
        minimalRespawnTime = RNG.nextInt(20, 30);
        timeFromUse = -1;
        isUsed = false;
    }

    /**
     * Oznacza zasób jako zebrany (zużyty) i rozpoczyna odliczanie czasu do jego respawnu.
     * Zwraca wartości regeneracyjne, które powinny zostać przekazane agentowi.
     *
     * @return Dwuelementowa tablica {@code int[]}, gdzie indeks [0] to wartość odzyskanej energii,
     * a indeks [1] to wartość odzyskanego zdrowia.
     */
    public int[] getUsed(){
        isUsed = true;
        timeFromUse = 0;
        return new int[] {energyRecovery, healthRecovery};
    }

    /**
     * Sprawdza bieżący stan dostępności zasobu na planszy.
     *
     * @return {@code true}, jeśli zasób został już zebrany i jest nieaktywny;
     * {@code false}, jeśli wciąż można go zebrać.
     */
    public boolean wasUsed(){
        return isUsed;
    }

    /**
     * Aktualizuje licznik czasu, jaki upłynął od momentu zebrania zasobu.
     * Metoda ta powinna być wywoływana cyklicznie w każdym kroku (ticku) symulacji.
     */
    public void updateTime(){
        timeFromUse += 1;
    }

    /**
     * Próbuje odrodzić zasób na planszy.
     * Szansa na pomyślny respawn rośnie wraz z upływem czasu od momentu jego zebrania.
     */
    public void Respawn(){
        if(RNG.nextInt(minimalRespawnTime) + timeFromUse > minimalRespawnTime){
            isUsed = false;
        }
    }
}