package org.example;

public class Bron extends Ekwipunek{
    private int bazowaSila;
    private int aktualnaWytrzymalosc;
    private int maxWytrzymalosc;

    public Bron(int bazowaSila, int maxWytrzymalosc)
    {
     this.bazowaSila = bazowaSila;
     this.maxWytrzymalosc = maxWytrzymalosc;
     this,aktualnaWytrzymalosc = aktualnaWytrzymalosc;
    }

    public void zmniejszWytrzymalosc()
    {
        if(aktualnaWytrzymalosc>0)
        {
            aktualnaWytrzymalosc--;
        }
    }

    public int obliczAktualnaSile()
    {
        if(maxWytrzymalosc==0) {return 0;}
        else
        {
            return (int) Math.round((double) (bazowaSila*aktualnaWytrzymalosc)/maxWytrzymalosc)
        }
    }

    public int getBazowaSila()
    {
        return bazowaSila;
    }

    public void setBazowaSila(int nowaSila)
    {
        this.bazowaSila = nowaSila;
    }

    public int getAktualnaWytrzymalosc()
    {
        return aktualnaWytrzymalosc;
    }

    public void setAktualnaWytrzymalosc(int nowaWytrzymalosc)
    {
        this.aktualnaWytrzymalosc = Math.max(0, Math.min(nowaWytrzymalosc,maxWytrzymalosc));
    }

    public int getMaxWytrzymalosc()
    {
        return maxWytrzymalosc;
    }

    public void setMaxWytrzymalosc(int maxWytrzymalosc)
    {
        this.maxWytrzymalosc = maxWytrzymalosc;
    }

    @Override
    public String toString()
    {
        return {"Bron(str: "+bazowaSila+"dur: "+aktualnaWytrzymalosc + "/" + maxWytrzymalosc + ")";}
    }
}
