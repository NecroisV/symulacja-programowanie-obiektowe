package org.example;

import java.util.Random;

public class EquipmentFactory {

    public enum EquipmentType {
        WEAPON, CLOTHES
    }

    public static Equipment createRandom(EquipmentType type) {
        Random random = new Random();
        return switch (type) {
            case WEAPON -> new Weapon(
                    random.nextInt(5) + 3,   // baseStrength: 3-7
                    random.nextInt(10) + 10  // maxDurability: 10-19
            );
            case CLOTHES -> {
                int maxPrevention = random.nextInt(5) + 3; // 3-7
                yield new Clothes(
                        random.nextInt(3) + 1,  // damageReduction: 1-3
                        random.nextInt(2) + 1,  // energyUseReduction: 1-2
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