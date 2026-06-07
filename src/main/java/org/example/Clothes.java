package org.example;

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

    private int calculateDamageReduction()
    {
        return (int) Math.round((double) (damageReduction * infectionPrevention)/ maxInfectionPrevention);
    }

    private int calculateEnergyUseReduction()
    {
        return (int) Math.round((double) (energyUseReduction * infectionPrevention)/ maxInfectionPrevention);
    }

    private void loseInfectionPrevention()
    {
        if(infectionPrevention >0)
        {
            infectionPrevention--;
        }
    }

    public int getDamageReduction()
    {
        return calculateDamageReduction();
    }

    public int getEnergyUseReduction()
    {
        return calculateEnergyUseReduction();
    }

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
