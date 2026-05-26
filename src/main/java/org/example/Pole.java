package org.example;

import java.util.ArrayList;
import java.util.List;

public class Pole {
    private int x;
    private int y;
    private boolean czySciana;
    private ZasobSrodowiskowy zasob;
    private List<Agent> agenci = new ArrayList<>();

    private Pole up;
    private Pole right;
    private Pole down;
    private Pole left;

    public Pole(int given_x, int given_y){
        x = given_x;
        y = given_y;
    }

    public float obliczWageDlaOcalalego(Ocalaly o, SrodowiskoSymulacji s){
        return 0.0f;
    }

    public float obliczWageDlaZakazonego(Zakazony z, SrodowiskoSymulacji s){
        return 0.0f;
    }

    public void stworzSciane(){czySciana=true;}

    public void zniszczSciane(){
    }

    public boolean czyJestWSafeZone(){
        return false;
    }

    public boolean czyJestSciana(){return czySciana;}

    public void polaczUp(Pole given_up){
        up = given_up;
    }
    public void polaczRight(Pole given_right){
        right = given_right;
    }
    public void polaczDown(Pole given_down){
        down = given_down;
    }
    public void polaczLeft(Pole given_left){
        left = given_left;
    }

    public void dodajAgneta(Agent agent){
        agenci.add(agent);
    }
    public void usunAgenta(Agent agent){
        agenci.remove(agent);
    }

    public List<Agent> przekazAgentow(){return agenci;}

    public int[] przkazPozycje(){return new int[]{x, y};}

    public Pole przekazUp(){return up;}
    public Pole przekazRight(){return right;}
    public Pole przekazDown(){return down;}
    public Pole przekazLeft(){return left;}

}
