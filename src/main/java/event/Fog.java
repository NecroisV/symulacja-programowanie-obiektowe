package event;

import simulation.RNG;
import simulation.SimulationParameters;
import simulation.Space;

/**
 * Klasa reprezentująca zdarzenie losowe w postaci mgły.
 * Jest to jedno ze zdarzeń implementujących interfejs {@link Event}.
 * Efektem mgły jest ograniczenie widoczności na planszy symulacji, co bezpośrednio
 * wpływa na zasięg percepcji/wzroku agentów w trakcie trwania anomalii.
 */
public class Fog implements Event {

    /**
     * Dwuelementowa tablica określająca zakres intensywności mgły (indeks [0] - wartość minimalna,
     * indeks [1] - wartość maksymalna), pobierana z globalnych parametrów symulacji.
     */
    private static final double[] fogIntensity = SimulationParameters.getInstance().getFogIntensity();

    /**
     * Domyślny konstruktor inicjalizujący zdarzenie mgły.
     */
    public Fog(){}

    /**
     * Wyzwala efekt mgły w symulacji.
     * Metoda losuje intensywność mgły z zadanego przedziału konfiguracji, wylicza końcowy
     * modyfikator widoczności (odejmując wylosowaną wartość od 1.0) i aplikuje go globalnie
     * poprzez statyczną metodę klasy {@link TimeOfDay}.
     *
     * @param board Dwuwymiarowa tablica obiektów {@link Space} reprezentująca aktualną siatkę planszy symulacji.
     */
    @Override
    public void trigger(Space[][] board) {
        double visibilityModifier = 1.0 - RNG.nextDouble(fogIntensity[0], fogIntensity[1]);
        TimeOfDay.setFogLevel(visibilityModifier);
    }
}