package org.example;

// Ubranie jako ekwipunek - zapewnia redukcję obrażeń, redukcję zużycia energii i ochronę przed infekcją
public class Clothes extends Equipment
{
    private int damageReduction;
    private int energyUseReduction;
    private int infectionPrevention;
    private final int maxInfectionPrevention;

    public Clothes(int damageReduction, int energyUseReduction, int infectionPrevention, int maxInfectionPrevention)
    {
        this.damageReduction = damageReduction;
        this.energyUseReduction = energyUseReduction;
        this.infectionPrevention = infectionPrevention;
        this.maxInfectionPrevention = maxInfectionPrevention;
    }

    // Oblicza aktualną redukcję obrażeń (uwzględnia zużycie)
    private int calculateDamageReduction()
    {
        return (int) Math.round((double) (damageReduction * infectionPrevention)/ maxInfectionPrevention);
    }

    // Oblicza aktualną redukcję zużycia energii (uwzględnia zużycie)
    private int calculateEnergyUseReduction()
    {
        return (int) Math.round((double) (energyUseReduction * infectionPrevention)/ maxInfectionPrevention);
    }

    // Zmniejsza poziom ochrony przed infekcją przy użyciu
    private void loseInfectionPrevention()
    {
        if(infectionPrevention >0)
        {
            infectionPrevention--;
        }
    }
    //Gettery
    public int getDamageReduction()
    {
        return calculateDamageReduction();
    }

    public int getEnergyUseReduction()
    {
        return calculateEnergyUseReduction();
    }

    // Próbuje użyć ochrony - zwraca true jeśli ochrona była dostępna (została zużyta)
    public boolean getInfectionPrevention()
    {
        if(infectionPrevention>0){
            loseInfectionPrevention();
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public String toString()
    {
        return ("Clothes(dmgRed: " + damageReduction + " energyRed: " + energyUseReduction
                + " infectionDef: " + infectionPrevention + "/" + maxInfectionPrevention +")");
    }
}