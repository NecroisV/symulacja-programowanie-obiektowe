package org.example;

import java.util.List;

public abstract class Agent {
    private int x;
    private int y;
    private int wiek;
    private int poziomZdrowia;
    private int bazowaSila;
    private int bazowePoleWidzenia;
    private int bazowaPredkosc;
    private boolean czyZywy;
    private List<Rana> rany;

    protected Agent(){

    }

    public void wykonajRuch(Pole cel){

    }

    public void starzejSie(){

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
}
