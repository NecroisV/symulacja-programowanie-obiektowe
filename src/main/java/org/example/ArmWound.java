package org.example;

public class ArmWound extends Wound {
    private int strengthReduction;

    public ArmWound(){
        strengthReduction = RNG.nextInt(1, 4);
    }

    public int getReduction(){return strengthReduction;}
}
