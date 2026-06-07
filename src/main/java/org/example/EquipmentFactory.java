package org.example;

public class EquipmentFactory {

    public enum EquipmentType {
        WEAPON, CLOTHES
    }

    public static Equipment createRandom(EquipmentType type) {
        return switch (type) {
            case WEAPON -> new Weapon(
                    RNG.nextInt(5) + 3,   // baseStrength: 3-7
                    RNG.nextInt(10) + 10  // maxDurability: 10-19
            );
            case CLOTHES -> {
                int maxPrevention = RNG.nextInt(5) + 3; // 3-7
                yield new Clothes(
                        RNG.nextInt(3) + 1,  // damageReduction: 1-3
                        RNG.nextInt(2) + 1,  // energyUseReduction: 1-2
                        maxPrevention,
                        maxPrevention
                );
            }
        };
    }

    public static Weapon createWeapon(int baseStrength, int maxDurability) {
        return new Weapon(baseStrength, maxDurability);
    }

    public static Clothes createClothes(int damageReduction, int energyUseReduction, int maxInfectionPrevention) {
        return new Clothes(damageReduction, energyUseReduction, maxInfectionPrevention, maxInfectionPrevention);
    }
}