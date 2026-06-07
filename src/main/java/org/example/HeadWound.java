package org.example;

public class HeadWound extends Wound {
    private int visibilityReduction;

    public HeadWound(){
        visibilityReduction = RNG.nextInt(1, 5);
    }

    public int getReduction(){return visibilityReduction;}
}
