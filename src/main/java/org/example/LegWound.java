package org.example;

// Rana nogi - zmniejsza prędkość poruszania się agenta
public class LegWound extends Wound {
    private int speedReduction;

    // Tworzy ranę z losową redukcją prędkości (1-3)
    public LegWound(){
        speedReduction = RNG.nextInt(1,4);
    }

    public int getReduction(){return speedReduction;}
}