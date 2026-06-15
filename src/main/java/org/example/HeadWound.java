package org.example;

// Rana głowy - zmniejsza pole widzenia agenta
public class HeadWound extends Wound {
    private int visibilityReduction;

    // Tworzy ranę z losową redukcją widoczności
    public HeadWound(){
        visibilityReduction = RNG.nextInt(1, 5);
    }

    public int getReduction(){return visibilityReduction;}
}