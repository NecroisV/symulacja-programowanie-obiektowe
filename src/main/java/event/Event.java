package event;

import simulation.Space;

/**
 * Interfejs definiujący kontrakt dla wszystkich zdarzeń losowych i środowiskowych w symulacji.
 * Klasy implementujące ten interfejs odpowiadają za wprowadzanie nagłych zmian na planszy
 * (np. modyfikację struktury pól, niszczenie ścian), które wpływają na przebieg symulacji.
 */
public interface Event {

    /**
     * Wyzwala unikalny efekt danego zdarzenia na planszy symulacji.
     * Metoda bezpośrednio modyfikuje stan poszczególnych pól siatki na podstawie logiki zdarzenia.
     *
     * @param board Dwuwymiarowa tablica obiektów {@link Space} reprezentująca aktualną siatkę planszy.
     */
    void trigger(Space[][] board);
}