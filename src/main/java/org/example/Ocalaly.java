package org.example;

import java.util.List;

public class Ocalaly extends Agent{
    private int poziomEnergii;
    private List<Pole> pamiecOdwiedzonychPol;
    private int pojemnoscEkwipunku;
    private List<Ekwipunek> ekwipunek;
    private float szansaNaWyleczenie;
    private boolean czyGloduje;

    public Ocalaly(){
        super();
    }

    public void zmienPoziomEnergii(int ilosc){

    }

    public void skonsumujZasob(ZasobSrodowiskowy zasob){

    }

    public void podniesEkwipunek(Ekwipunek ekw){

    }

    public boolean czyMaWolnySlot(){
        return true;
    }

    public void gloduj(){

    }

    public void walczZOcalalym(Ocalaly inny){

    }

    public void walczZZakazonym(Zakazony z){

    }

    public void okradnij(Ocalaly przegrany){

    }

    public Zakazony transformujWZakazonego(){
        return new Zakazony();
    }
}
