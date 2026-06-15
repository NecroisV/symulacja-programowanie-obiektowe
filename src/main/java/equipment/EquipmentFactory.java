package equipment;

import simulation.RNG;

/**
 * Wzorzec projektowy fabryki (Factory) odpowiedzialny za tworzenie obiektów ekwipunku.
 * Klasa umożliwia generowanie w pełni losowych przedmiotów na podstawie określonego typu
 * bądź tworzenie konkretnych instancji broni i ubrań o ściśle zdefiniowanych parametrach.
 */
public class EquipmentFactory {

    /**
     * Dostępne typy ekwipunku, które fabryka potrafi wyprodukować.
     */
    public enum EquipmentType {
        /** Reprezentuje przedmiot typu broń ({@link Weapon}). */
        WEAPON,

        /** Reprezentuje przedmiot typu ubranie ({@link Clothes}). */
        CLOTHES
    }

    /**
     * Tworzy losowy egzemplarz ekwipunku zadanego typu.
     * Metoda automatycznie losuje parametry przedmiotu w bezpiecznych dla balansu symulacji przedziałach.
     *
     * @param type Typ ekwipunku do wygenerowania ({@link EquipmentType#WEAPON} lub {@link EquipmentType#CLOTHES}).
     * @return Nowo utworzony obiekt klasy pochodnej {@link Equipment} pasujący do wybranego typu.
     */
    public static Equipment createRandom(EquipmentType type) {
        return switch (type) {
            case WEAPON -> {
                int max = RNG.nextInt(10) + 10;     // Maksymalna wytrzymałość 10-19
                int actual = RNG.nextInt(max) + 1;  // Aktualna wytrzymałość 1-max
                yield new Weapon(RNG.nextInt(5) + 3, actual, max); // Siła zależna od RNG
            }
            case CLOTHES -> {
                int maxPrevention = RNG.nextInt(5) + 3; // Ochrona przed infekcją zależne od RNG
                yield new Clothes(
                        RNG.nextInt(3) + 1,  // damageReduction zależne od RNG
                        RNG.nextInt(2) + 1,  // energyUseReduction zależne od RNG
                        maxPrevention,
                        maxPrevention
                );
            }
        };
    }

    /**
     * Tworzy instancję broni o konkretnych, podanych parametrach.
     * Metoda zachowana w celu zapewnienia kompatybilności wstecznej oraz testowania.
     *
     * @param baseStrength      Bazowa siła zadawana przez broń.
     * @param actualDurability  Aktualny poziom wytrzymałości broni.
     * @param maxDurability     Maksymalny możliwy poziom wytrzymałości broni.
     * @return Nowy obiekt klasy {@link Weapon}.
     */
    public static Weapon createWeapon(int baseStrength, int actualDurability, int maxDurability) {
        return new Weapon(baseStrength,actualDurability, maxDurability);
    }

    /**
     * Tworzy instancję ubrania o konkretnych, podanych parametrach.
     * Metoda domyślnie ustawia aktualną ochronę przed infekcją na wartość maksymalną.
     * Metoda zachowana w celu zapewnienia kompatybilności wstecznej oraz testowania.
     *
     * @param damageReduction        Wartość redukcji obrażeń zadawanych agentowi.
     * @param energyUseReduction      Wartość redukcji zużycia energii przez agenta.
     * @param maxInfectionPrevention Maksymalny (i zarazem początkowy) poziom ochrony przed infekcją.
     * @return Nowy obiekt klasy {@link Clothes}.
     */
    public static Clothes createClothes(int damageReduction, int energyUseReduction, int maxInfectionPrevention) {
        return new Clothes(damageReduction, energyUseReduction, maxInfectionPrevention, maxInfectionPrevention);
    }
}