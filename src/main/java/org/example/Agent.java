package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;

public abstract class Agent {
    private int x;
    private int y;
    private int wiek = 0;
    private int poziomZdrowia;
    private int bazowaSila;
    private int bazowePoleWidzenia;
    private int bazowaPredkosc = 3;
    private boolean czyZywy = true;
    private List<Rana> rany;

    protected Agent(int given_x, int given_y, int given_zdrowie){
        x = given_x;
        y = given_y;
        poziomZdrowia = given_zdrowie;
    }

    public int[] wykonajRuch(Pole start){
        ArrayList<Pole> mozliwePola = mozliwyRuch(start);

        Random random = new Random();
        Pole docelowePole = mozliwePola.get(random.nextInt(mozliwePola.size()));
        return docelowePole.przkazPozycje();
    }

    private ArrayList<Pole> mozliwyRuch(Pole start) {
        ArrayList<Pole> odwiedzonePola = new ArrayList<>();

        if (start.czyJestSciana()) {
            return odwiedzonePola;
        }

        ArrayList<Pole> kolejka = new ArrayList<>();
        ArrayList<Integer> odleglosci = new ArrayList<>();

        kolejka.add(start);
        odleglosci.add(0);
        odwiedzonePola.add(start);

        int analizowanyIndeks = 0;

        while (analizowanyIndeks < kolejka.size()) {
            Pole aktualne = kolejka.get(analizowanyIndeks);
            int aktualnaOdleglosc = odleglosci.get(analizowanyIndeks);
            analizowanyIndeks++;

            if (aktualnaOdleglosc >= bazowaPredkosc) {
                continue;
            }

            Pole[] sasiedzi = {aktualne.przekazUp(), aktualne.przekazRight(), aktualne.przekazDown(), aktualne.przekazLeft()};

            for (Pole sasiad : sasiedzi) {
                if (sasiad != null && !sasiad.czyJestSciana() && !odwiedzonePola.contains(sasiad)) {
                    kolejka.add(sasiad);
                    odleglosci.add(aktualnaOdleglosc + 1);
                    odwiedzonePola.add(sasiad);
                }
            }
        }

        return odwiedzonePola;
    }

    public void starzejSie(){
        wiek++;
    }

    public void umrzyj(){

    }

    public void zmienPoziomZdrowia(int ilosc){

    }

    public int obliczPoleWidzenia(float poziomOswietlenia){
        return 0;
    }

    public int obliczSile(){
        return 0;
    }

    public int obliczPredkosc(){
        return 0;
    }

    public boolean czyJestZywy(){return czyZywy;}

    public int[] przekazPozycje(){
        return new int[]{x, y};
    }

    public int przekazZdrowie(){return poziomZdrowia;}

    public void otrzymajRane(){

    }
}
