package org.example;

public class Ubranie extends Ekwipunek
{
    private int redukcjaObrazen;
    private int redukcjaZuzyciaEnergii;
    private int obronaPrzedZakazeniem;
    private int maxObronaPrzedZakazeniem;

    public Ubranie(int redukcjaObrazen, int redukcjaZuzyciaEnergii, int obronaPrzedZakazeniem, int maxObronaPrzedZakazeniem)
    {
        this.redukcjaObrazen = redukcjaObrazen;
        this.redukcjaZuzyciaEnergii = redukcjaZuzyciaEnergii;
        this.obronaPrzedZakazeniem = obronaPrzedZakazeniem;
        this.maxObronaPrzedZakazeniem = maxObronaPrzedZakazeniem; //wartosc wejsciowa do zapamietania
    }

    public int obliczAktualnaRedukcjeObrazen()
    {
        if(maxObronaPrzedZakazeniem<=0) {return 0;}
        return (int) Math.round((double) (redukcjaObrazen*obronaPrzedZakazeniem)/maxObronaPrzedZakazeniem);
    }

    public int obliczAktualnaRedukcjeZuzyciaEnergii()
    {
        if(maxObronaPrzedZakazeniem<=0) {return 0;}
        else
        {
            return (int) Math.round((double) (redukcjaZuzyciaEnergii*obronaPrzedZakazeniem)/maxObronaPrzedZakazeniem);
        }
    }
    public void zmniejszObronePrzedZakazeniem()
    {
        if(obronaPrzedZakazeniem>0)
        {
            obronaPrzedZakazeniem--;
        }
    }

    public int getRedukcjaObrazen()
    {
        return redukcjaObrazen;
    }

    public void setRedukcjaObrazen(int nowaRedukcjaObrazen)
    {
        this.redukcjaObrazen = nowaRedukcjaObrazen;
    }

    public int getRedukcjaZuzyciaEnergii()
    {
        return redukcjaZuzyciaEnergii;
    }

    public void setRedukcjaZuzyciaEnergii(int nowaRedukcjaZuzyciaEnergii)
    {
        this.redukcjaZuzyciaEnergii = nowaRedukcjaZuzyciaEnergii;
    }

    public int getObronaPrzedZakazeniem()
    {
        return obronaPrzedZakazeniem;
    }

    public void setObronaPrzedZakazeniem(int nowaObronaPrzedZakarzeniem)
    {
        if(nowaObronaPrzedZakarzeniem>=0 && nowaObronaPrzedZakarzeniem <= maxObronaPrzedZakazeniem)
        {
            this.obronaPrzedZakazeniem = nowaObronaPrzedZakarzeniem;
        }
    }

    public int getMaxObronaPrzedZakazeniem()
    {
        return maxObronaPrzedZakazeniem;
    }

    @Override
    public String toString()
    {
        return ("Ubranie(dmgRed: " + redukcjaObrazen + " energyRed: " + redukcjaZuzyciaEnergii
                + " infectionDef: " + obronaPrzedZakazeniem+ "/" +maxObronaPrzedZakazeniem+")");
    }
}
