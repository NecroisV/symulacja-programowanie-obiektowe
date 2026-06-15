package agent;

import simulation.RNG;

/**
 * Klasa reprezentująca ranę ręki agenta.
 * Tego typu rana wpływa negatywnie na zdolności bojowe, zmniejszając aktualną siłę agenta.
 */
public class ArmWound extends Wound {

    /** Wartość, o jaką redukowana jest siła agenta. */
    private int strengthReduction;

    /**
     * Konstruktor tworzący ranę ręki.
     * Automatycznie losuje wartość redukcji siły w przedziale od 1 do 3 włącznie.
     */
    public ArmWound(){
        strengthReduction = RNG.nextInt(1, 4);
    }

    /**
     * Zwraca wartość, o którą należy pomniejszyć siłę agenta z powodu tej rany.
     *
     * @return Liczba punktów redukcji siły.
     */
    public int getReduction(){return strengthReduction;}
}