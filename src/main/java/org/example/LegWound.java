package org.example;

public class LegWound extends Wound {
    private int speedReduction;

    public LegWound(){
        speedReduction = RNG.nextInt(1,4);
    }

    public int getReduction(){return speedReduction;}
}
