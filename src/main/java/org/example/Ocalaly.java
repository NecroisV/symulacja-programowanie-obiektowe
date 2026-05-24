package org.example;

import java.util.List;
import static java.lang.Math.round;

public class Ocalaly extends Agent{
    private int poziomEnergii = 100;
    private List<Pole> pamiecOdwiedzonychPol;
    private int pojemnoscEkwipunku;
    private List<Ekwipunek> ekwipunek;
    private float szansaNaWyleczenie;
    private boolean czyGloduje = false;

    public Ocalaly(int given_x, int given_y, int given_zdrowie){
        super(given_x, given_y, given_zdrowie);
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

    public Zakazony transformujWZakazonego(Zakazony z){
        int[] pozycja = przekazPozycje();

        return new Zakazony(pozycja[0], pozycja[1], (int) round(z.przekazZdrowie() / 2.0));
    }
}
