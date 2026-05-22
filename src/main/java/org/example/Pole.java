package org.example;

import java.util.ArrayList;
import java.util.List;

public class Pole {
    private int x;
    private int y;
    private boolean czySciana;
    private ZasobSrodowiskowy zasob;
    private List<Agent> agenci = new ArrayList<>();

    public Pole(){

    }

    public float obliczWageDlaOcalalego(Ocalaly o, SrodowiskoSymulacji s){
        return 0.0f;
    }

    public float obliczWageDlaZakazonego(Zakazony z, SrodowiskoSymulacji s){
        return 0.0f;
    }

    public void zniszczSciane(){

    }

    public boolean czyJestWSafeZone(){
        return false;
    }
}
