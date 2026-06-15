package event;

import simulation.SimulationParameters;

/**
 * Klasa narzędziowa zarządzająca globalnym czasem dobowym oraz modelem widoczności na planszy.
 * Odpowiada za symulację płynnych przejść tonalnych pomiędzy dniem a nocą (świt, zmierzch)
 * oraz obsługę dynamicznych zmian środowiskowych, takich jak redukcja widoczności przez mgłę.
 * <p>
 * Poziom oświetlenia/widoczności jest wyliczany deterministycznie na podstawie aktualnego kroku
 * symulacji (ticka) przy użyciu interpolacji cosinusoidalnej, co zapobiega skokowym zmianom
 * percepcji u agentów z tury na turę.
 * </p>
 */
public final class TimeOfDay {

    /** Czas trwania fazy dziennej wyrażony w tickach symulacji. */
    private static int durationDay = SimulationParameters.getInstance().getDayNightCycle()[0];

    /** Czas trwania fazy nocnej wyrażony w tickach symulacji. */
    private static int durationNight = SimulationParameters.getInstance().getDayNightCycle()[1];

    /** Łączny czas trwania pełnego cyklu dobowego (dzień + noc) w tickach. */
    private static int durationTotal = durationDay + durationNight;

    /** * Współczynnik tłumienia widoczności wywołany obecnością mgły.
     * Wartość {@code 1.0} oznacza całkowity brak mgły (pełna przejrzystość).
     * Wartości mniejsze od 1 redukują pole widzenia.
     */
    private static double fogLevel = 1;

    /** Minimalny bazowy poziom widoczności osiągany w samym środku nocy. */
    private static double minVisibility = 0.5;

    /** Maksymalny bazowy poziom widoczności osiągany w samym środku dnia. */
    private static double maxVisibility = 1.2;

    /**
     * Oblicza dynamiczny współczynnik widoczności dla konkretnego kroku (ticka) symulacji.
     * <p>
     * <b>Matematyczny opis algorytmu:</b>
     * 1. Za pomocą operatora modulo {@code currentTick % durationTotal} wyznaczany jest aktualny czas wewnątrz pętli dobowej.
     * 2. Wyliczany jest punkt szczytowy dnia (południe): {@code (durationDay - 1) / 2.0}.
     * 3. Różnica czasu od punktu szczytowego jest mapowana na kąt w radianach, dla którego obliczana jest funkcja {@link Math#cos(double)}.
     * 4. Wynik funkcji cosinus (zakres {@code [-1.0, 1.0]}) zostaje znormalizowany do przedziału {@code [0.0, 1.0]}.
     * 5. Znormalizowana wartość służy do liniowej interpolacji pomiędzy {@code minVisibility} a {@code maxVisibility}.
     * 6. Wyjściowy poziom jest modyfikowany przez aktualny stan mgły ({@code fogLevel}).
     * 7. Ostateczny wynik jest ograniczany od dołu (clamping) do wartości **0.2 (20%)**, aby zapewnić agentom minimalną zdolność percepcji nawet w skrajnych warunkach nocnych lub przy gęstej mgle.
     * </p>
     *
     * @param currentTick Globalny, porządkowy numer aktualnego kroku symulacji.
     * @return Współczynnik widoczności (zazwyczaj z przedziału {@code [0.2, 1.2]}), przez który silnik symulacji mnoży bazowy zasięg FOV agentów.
     */
    public static double getVisibilityLevel(int currentTick) {
        int tickInCycle = currentTick % durationTotal;
        double peakTick = (durationDay - 1) / 2.0;

        // Transformacja upływu czasu na wektor fali cosinusoidalnej
        double angle = 2 * Math.PI * (tickInCycle - peakTick) / durationTotal;
        double cosValue = Math.cos(angle);

        // Normalizacja z [-1, 1] do [0, 1]
        double normalizedValue = (cosValue + 1) / 2.0;

        // Interpolacja i nałożenie mnożnika anomalii pogodowej
        double visibility = (minVisibility + (maxVisibility - minVisibility) * normalizedValue) * fogLevel;

        return Math.max(0.2, visibility);
    }

    /**
     * Moduluje intensywność mgły środowiskowej na planszy.
     * Metoda jest wywoływana przez system zdarzeń losowych (np. klasę reprezentującą anomalię mgły)
     * w celu dynamicznego ograniczenia percepcji sensorycznej jednostek.
     *
     * @param fog Nowa wartość współczynnika mgły (np. {@code 0.4} dla gęstej mgły, {@code 1.0} dla czystego nieba).
     */
    public static void setFogLevel(double fog) {
        fogLevel = fog;
    }
}
