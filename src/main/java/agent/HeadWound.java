package agent;

import simulation.RNG;

/**
 * Klasa reprezentująca ranę głowy agenta.
 * Tego typu rana wpływa negatywnie na percepcję, bezpośrednio ograniczając pole widzenia
 * (zasięg wzroku) zranionego agenta.
 */
public class HeadWound extends Wound {

    /** Wartość, o jaką pomniejszany jest zasięg widzenia agenta. */
    private int visibilityReduction;

    /**
     * Konstruktor tworzący ranę głowy.
     * Automatycznie losuje wartość redukcji pola widzenia w przedziale od 1 do 4 włącznie.
     */
    public HeadWound(){
        visibilityReduction = RNG.nextInt(1, 5);
    }

    /**
     * Zwraca wartość, o którą należy pomniejszyć zasięg wzroku agenta z powodu tej rany.
     *
     * @return Liczba punktów redukcji widoczności.
     */
    public int getReduction(){return visibilityReduction;}
}