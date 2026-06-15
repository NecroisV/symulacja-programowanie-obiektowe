package org.example;

// Rana ręki - zmniejsza siłę ataku agenta
public class ArmWound extends Wound {
    private int strengthReduction;

    // Tworzy ranę z losową redukcją siły (1-3)
    public ArmWound(){
        strengthReduction = RNG.nextInt(1, 4);
    }

    public int getReduction(){return strengthReduction;}
}