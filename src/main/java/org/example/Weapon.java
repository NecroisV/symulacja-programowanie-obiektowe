package org.example;

public class Weapon extends Equipment {
    private int baseStrength;
    private int actualDurability;
    private int maxDurability;

    public Weapon(int baseStrength, int maxDurability)
    {
     this.baseStrength = baseStrength;
     this.maxDurability = maxDurability;
     this.actualDurability = actualDurability;
    }

    public void loseDurability()
    {
        if(actualDurability >0)
        {
            actualDurability--;
        }
    }

    public int calculateActualStrength()
    {
        if(maxDurability ==0) {return 0;}
        else
        {
            return (int) Math.round((double) (baseStrength * actualDurability)/ maxDurability);
        }
    }

    public int getBaseStrength()
    {
        return baseStrength;
    }

    public void setBaseStrength(int newStrength)
    {
        this.baseStrength = newStrength;
    }

    public int getActualDurability()
    {
        return actualDurability;
    }

    public void setActualDurability(int newDurability)
    {
        this.actualDurability = Math.max(0, Math.min(newDurability, maxDurability));
    }

    public int getMaxDurability()
    {
        return maxDurability;
    }

    public void setMaxDurability(int maxDurability)
    {
        this.maxDurability = maxDurability;
    }

    @Override
    public String toString()
    {
        return "Weapon(strength: " + baseStrength + " durability: " + actualDurability + "/" + maxDurability + ")";
    }
}
