package equipment;

/**
 * Klasa reprezentująca ubranie jako element ekwipunku agenta (np. ocalałego).
 * Zapewnia dynamicznie obliczaną redukcję obrażeń, redukcję zużycia energii
 * oraz ograniczoną ochronę przed infekcją, która zużywa się wraz z użytkowaniem.
 */
public class Clothes extends Equipment
{
    /** Bazowa wartość redukcji obrażeń. */
    private int damageReduction;

    /** Bazowa wartość redukcji zużycia energii przez agenta. */
    private int energyUseReduction;

    /** Aktualny poziom ochrony przed infekcją (pełni również rolę "wytrzymałości" przedmiotu). */
    private int infectionPrevention;

    /** Maksymalny (początkowy) poziom ochrony przed infekcją. */
    private final int maxInfectionPrevention;

    /**
     * Tworzy nowy element ubrania o zadanych parametrach.
     *
     * @param damageReduction        Maksymalna redukcja obrażeń.
     * @param energyUseReduction      Maksymalna redukcja zużycia energii.
     * @param infectionPrevention    Początkowy poziom ochrony przed infekcją.
     * @param maxInfectionPrevention Maksymalny możliwy poziom ochrony przed infekcją dla tego ubrania.
     */
    public Clothes(int damageReduction, int energyUseReduction, int infectionPrevention, int maxInfectionPrevention)
    {
        this.damageReduction = damageReduction;
        this.energyUseReduction = energyUseReduction;
        this.infectionPrevention = infectionPrevention;
        this.maxInfectionPrevention = maxInfectionPrevention;
    }

    /**
     * Oblicza aktualną redukcję obrażeń proporcjonalnie do stopnia zużycia ubrania.
     * Im mniejsza ochrona przed infekcją (infectionPrevention), tym gorsza ochrona przed obrażeniami.
     *
     * @return Skalowana wartość redukcji obrażeń jako liczba całkowita (zaokrąglona).
     */
    private int calculateDamageReduction()
    {
        return (int) Math.round((double) (damageReduction * infectionPrevention)/ maxInfectionPrevention);
    }

    /**
     * Oblicza aktualną redukcję zużycia energii proporcjonalnie do stopnia zużycia ubrania.
     *
     * @return Skalowana wartość redukcji zużycia energii jako liczba całkowita (zaokrąglona).
     */
    private int calculateEnergyUseReduction()
    {
        return (int) Math.round((double) (energyUseReduction * infectionPrevention)/ maxInfectionPrevention);
    }

    /**
     * Zmniejsza poziom ochrony przed infekcją o 1, reprezentując stopniowe niszczenie się ubrania.
     * Wartość nie spadnie poniżej zera.
     */
    private void loseInfectionPrevention()
    {
        if(infectionPrevention > 0)
        {
            infectionPrevention--;
        }
    }

    /**
     * Zwraca aktualną wartość redukcji obrażeń po uwzględnieniu stopnia zużycia.
     *
     * @return Aktualna redukcja obrażeń.
     */
    public int getDamageReduction()
    {
        return calculateDamageReduction();
    }

    /**
     * Zwraca aktualną wartość redukcji zużycia energii po uwzględnieniu stopnia zużycia.
     *
     * @return Aktualna redukcja zużycia energii.
     */
    public int getEnergyUseReduction()
    {
        return calculateEnergyUseReduction();
    }

    /**
     * Próbuje wykorzystać ubranie do zablokowania infekcji.
     * <b>Uwaga:</b> Wywołanie tej metody, przy aktywnej ochronie, trwale zmniejsza jej poziom (zużywa przedmiot).
     *
     * @return {@code true}, jeśli ubranie posiadało jeszcze punkty ochrony i zablokowało infekcję (zostało zużyte);
     * {@code false} w przeciwnym razie.
     */
    public boolean getInfectionPrevention()
    {
        if(infectionPrevention > 0){
            loseInfectionPrevention();
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Zwraca tekstową reprezentację obiektu zawierającą informacje o jego aktualnych statystykach.
     *
     * @return Ciąg znaków opisujący stan ubrania.
     */
    @Override
    public String toString()
    {
        return ("Clothes(dmgRed: " + damageReduction + " energyRed: " + energyUseReduction
                + " infectionDef: " + infectionPrevention + "/" + maxInfectionPrevention +")");
    }
}