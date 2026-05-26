package org.example;

public class Cloths extends Equipment
{
    private int damageReduction;
    private int energyUseReduction;
    private int infectionPrevention;
    private int maxInfectionPrevention;

    public Cloths(int damageReduction, int energyUseReduction, int infectionPrevention, int maxInfectionPrevention)
    {
        this.damageReduction = damageReduction;
        this.energyUseReduction = energyUseReduction;
        this.infectionPrevention = infectionPrevention;
        this.maxInfectionPrevention = maxInfectionPrevention; //wartosc wejsciowa do zapamietania
    }

    public int calculateDamageReduction()
    {
        if(maxInfectionPrevention <=0) {return 0;}
        return (int) Math.round((double) (damageReduction * infectionPrevention)/ maxInfectionPrevention);
    }

    public int calculateEnergyUseReduction()
    {
        if(maxInfectionPrevention <=0) {return 0;}
        else
        {
            return (int) Math.round((double) (energyUseReduction * infectionPrevention)/ maxInfectionPrevention);
        }
    }
    public void loseInfectionPrevention()
    {
        if(infectionPrevention >0)
        {
            infectionPrevention--;
        }
    }

    public int getDamageReduction()
    {
        return damageReduction;
    }

    public void setDamageReduction(int newDamageReduction)
    {
        this.damageReduction = newDamageReduction;
    }

    public int getEnergyUseReduction()
    {
        return energyUseReduction;
    }

    public void setEnergyUseReduction(int newEnergyUseReduction)
    {
        this.energyUseReduction = newEnergyUseReduction;
    }

    public int getInfectionPrevention()
    {
        return infectionPrevention;
    }

    public void setInfectionPrevention(int newInfectionPrevention)
    {
        if(newInfectionPrevention >=0 && newInfectionPrevention <= maxInfectionPrevention)
        {
            this.infectionPrevention = newInfectionPrevention;
        }
    }

    public int getMaxInfectionPrevention()
    {
        return maxInfectionPrevention;
    }

    @Override
    public String toString()
    {
        return ("Cloths(dmgRed: " + damageReduction + " energyRed: " + energyUseReduction
                + " infectionDef: " + infectionPrevention + "/" + maxInfectionPrevention +")");
    }
}
