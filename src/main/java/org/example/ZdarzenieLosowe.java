package org.example;

public abstract class ZdarzenieLosowe {
    private int aktualnyCzasTrwania;
    private int maksymalnyCzasTrwania;
    private int czasOdOstatniegoWystapienia;
    private int minimalnyCzasDoNastepnegoWystapienia;
    private float szansaNaWystapienie;

    protected ZdarzenieLosowe(){

    }

    public void zastosujEfekt(SrodowiskoSymulacji srodowisko){

    }

    public boolean sprawdzWczesniejszeZakonczenie(){
        return true;
    }

    public boolean sprawdzCzyWystapi(){
        return true;
    }

    public void aktualizujCzas(){

    }
}
