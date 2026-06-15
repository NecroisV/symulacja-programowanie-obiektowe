package org.example;

// Fabryka do tworzenia losowego ekwipunku
public class EquipmentFactory {

    public enum EquipmentType {
        WEAPON, CLOTHES
    }

    // Tworzy losowy egzemplarz ekwipunku zadanego typu
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

    // Tworzy konkretną broń (zachowane dla kompatybilności)
    public static Weapon createWeapon(int baseStrength, int actualDurability, int maxDurability) {
        return new Weapon(baseStrength,actualDurability, maxDurability);
    }

    // Tworzy konkretne ubranie (zachowane dla kompatybilności)
    public static Clothes createClothes(int damageReduction, int energyUseReduction, int maxInfectionPrevention) {
        return new Clothes(damageReduction, energyUseReduction, maxInfectionPrevention, maxInfectionPrevention);
    }
}