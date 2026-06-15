package agent;

import simulation.RNG;

/**
 * Klasa reprezentująca ranę nogi agenta.
 * Tego typu rana wpływa negatywnie na mobilność agenta, bezpośrednio zmniejszając
 * jego maksymalną prędkość poruszania się (liczbę kroków na turę).
 */
public class LegWound extends Wound {

    /** Wartość, o jaką redukowana jest prędkość poruszania się agenta. */
    private int speedReduction;

    /**
     * Konstruktor tworzący ranę nogi.
     * Automatycznie losuje wartość redukcji prędkości w przedziale od 1 do 3 włącznie.
     */
    public LegWound(){
        speedReduction = RNG.nextInt(1,4);
    }

    /**
     * Zwraca wartość, o którą należy pomniejszyć prędkość agenta z powodu tej rany.
     *
     * @return Liczba punktów redukcji prędkości.
     */
    public int getReduction(){return speedReduction;}
}