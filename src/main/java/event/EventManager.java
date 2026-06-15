package event;

import simulation.RNG;
import simulation.SimulationParameters;
import simulation.Space;

/**
 * Menedżer zarządzający zdarzeniami losowymi i środowiskowymi w symulacji.
 * Klasa działa w sposób statyczny, kontrolując czas trwania aktywnych zdarzeń (np. burza, mgła, trzęsienie ziemi),
 * odliczając czas odpoczynku (cooldown) między nimi oraz wyliczając prawdopodobieństwo wystąpienia kolejnych anomalii.
 */
public class EventManager {
    /** Pozostały czas trwania (w tickach) aktualnie aktywnego zdarzenia. */
    private static int currentDuration = 0;

    /** Liczba ticków/tur, które upłynęły od momentu zakończenia ostatniego zdarzenia. */
    private static int sinceLastEvent = 0;

    /** Minimalny czas przerwy (w tickach) wymagany między zakończeniem jednego zdarzenia a szansą na rozpoczęcie kolejnego. */
    private static int minimumCooldown = 10;

    /** Referencja do aktualnie aktywnego obiektu zdarzenia. Wartość {@code null} oznacza brak aktywnych anomalii. */
    private static Event currentEvent = null;

    /** Tablica przechowująca prawdopodobieństwa i wagi losowania poszczególnych zdarzeń, pobrana z konfiguracji. */
    private static final double[] eventChances = SimulationParameters.getInstance().getEventChances();

    /** Bazowe prawdopodobieństwo wyzwolenia jakiegokolwiek zdarzenia w danym ticku po upływie czasu cooldownu. */
    private static final double chanceToSpawn = eventChances[0];

    /**
     * Konstruktor chroniony. Klasa projektowana jako zestaw metod statycznych (klasa narzędziowa),
     * dlatego bezpośrednia instancjalizacja spoza hierarchii lub pakietu jest ograniczona.
     */
    protected EventManager(){

    }

    /**
     * Sprawdza, czy w bieżącym ticku spełnione są warunki do wylosowania nowego zdarzenia.
     * Inkrementuje licznik czasu od ostatniego zdarzenia, sprawdza warunek minimalnego cooldownu
     * oraz przeprowadza test losowy (RNG) na podstawie bazowej szansy.
     *
     * @return {@code true}, jeśli warunki czasowe i losowe zostały spełnione; {@code false} w przeciwnym razie.
     */
    private static boolean canSpawnEvent(){
        sinceLastEvent++;
        if(sinceLastEvent >= minimumCooldown){
            double random = RNG.nextDouble();
            if(random <= chanceToSpawn){
                sinceLastEvent = 0;
                return true;
            }
        }
        return false;
    }

    /**
     * Próbuje wygenerować i aktywować nowe zdarzenie losowe.
     * Jeśli metoda {@link #canSpawnEvent()} zwróci wartość pozytywną, następuje kumulatywne losowanie
     * proporcjonalne do wag przypisanych do trzech typów zdarzeń: burzy (Thunderstorm), mgły (Fog)
     * oraz trzęsienia ziemi (Earthquake). Dla wybranego zdarzenia losowany jest również czas jego trwania.
     */
    private static void trySpawnEvent(){
        if(canSpawnEvent()){
            double weightSum = eventChances[1]+eventChances[2]+eventChances[3];
            double roll = RNG.nextDouble(0, weightSum);
            double thresholdStorm = eventChances[1];
            double thresholdFog = eventChances[1]+eventChances[2];
            double thresholdEarthquake = eventChances[1]+eventChances[2]+eventChances[3];
            if (roll <= thresholdStorm){
                currentEvent = new Thunderstorm();
                currentDuration = RNG.nextInt(SimulationParameters.getInstance().getEventDuration()[0], SimulationParameters.getInstance().getEventDuration()[1]);
            }
            else if (roll <= thresholdFog){
                currentEvent = new Fog();
                currentDuration = RNG.nextInt(SimulationParameters.getInstance().getEventDuration()[0], SimulationParameters.getInstance().getEventDuration()[1]);
            }
            else if (roll <= thresholdEarthquake){
                currentEvent = new Earthquake();
                currentDuration = RNG.nextInt(SimulationParameters.getInstance().getEventDuration()[0], SimulationParameters.getInstance().getEventDuration()[1]);
            }
            else {
                System.err.println("Z jakiegos powodu zaden event nie zostal wylosowany");
                currentEvent = null;
            }
        }
    }

    /**
     * Główna metoda kontrolna menedżera, wywoływana cyklicznie w każdym kroku (ticku) symulacji.
     * Odpowiada za pełny cykl życia zdarzenia:
     * <ul>
     * <li>Gdy brak aktywnego zdarzenia — próbuje uruchomić nowe.</li>
     * <li>Gdy zdarzenie jest aktywne — aplikuje jego efekty na planszę i dekrementuje pozostały czas trwania.</li>
     * <li>Gdy czas trwania dobiegnie końca — wyłącza zdarzenie i resetuje parametry środowiskowe (np. poziom mgły).</li>
     * </ul>
     *
     * @param board Dwuwymiarowa tablica obiektów {@link Space} reprezentująca siatkę planszy symulacji.
     */
    public static void runEventCheck(Space[][] board){
        if(currentEvent == null){
            trySpawnEvent();
        }
        else if(currentDuration > 0){
            currentEvent.trigger(board);
            currentDuration--;
            if(currentDuration == 0){
                TimeOfDay.setFogLevel(1);
                currentEvent = null;
            }
        }
    }

    /**
     * Zwraca aktualnie aktywne zdarzenie środowiskowe.
     *
     * @return Obiekt implementujący interfejs {@link Event} lub {@code null}, jeśli na planszy nie występuje żadna anomalia.
     */
    public static Event getCurrentEvent(){
        return currentEvent;
    }
}