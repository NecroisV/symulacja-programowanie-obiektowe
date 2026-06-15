package equipment;

import simulation.Space;

/**
 * Interfejs definiujący strategię rozmieszczania (spawnowania) ekwipunku na planszy symulacji.
 * Implementuje wzorzec projektowy Strategia (Strategy), co pozwala na tworzenie różnych
 * algorytmów dystrybucji przedmiotów (np. losowego, klastrowego, równomiernego) i łatwą ich wymianę.
 */
public interface EquipmentSpawnStrategy {

    /**
     * Rozmieszcza zadaną liczbę broni oraz ubrań na polach planszy symulacji.
     * Implementacja tej metody powinna określić algorytm wyboru odpowiednich,
     * wolnych przestrzeni do umieszczenia przedmiotów.
     *
     * @param board        Dwuwymiarowa tablica obiektów {@link Space} reprezentująca siatkę planszy.
     * @param weaponCount  Liczba sztuk broni ({@link Weapon}) do wygenerowania i rozłożenia.
     * @param clothesCount Liczba ubrań ({@link Clothes}) do wygenerowania i rozłożenia.
     */
    void spawnEquipment(Space[][] board, int weaponCount, int clothesCount);
}