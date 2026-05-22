package org.example;

import java.util.ArrayList;
import java.util.List;

public class SrodowiskoSymulacji {
    private Pole[][] plansza;
    private List<Agent> listaAgentow;
    private int aktualnyTick;
    private float poziomOswietlenia;
    private PoraDnia aktualnaPoraDnia;
    private ParametrySymulacji parametry;
    private ZbieraczMetryk metryki;
    private List<StrefaBezpieczna> strefy = new ArrayList<>();
    private List<ZdarzenieLosowe> zdarzeniaLosowe = new ArrayList<>();

    public SrodowiskoSymulacji(){

    }

    public void krokSymulacji(){

    }

    public void aktualizujPoreDnia(){

    }

    public float obliczPoziomOswietlenia(){
        return 0.0f;
    }

    public int[] zastosujGeometrieTorusa(int x, int y){
        return new int[]{0};
    }

    public void rozpatrzInterakcje(){

    }

    public void transformujOcalalego(Ocalaly o){

    }

    public void usunMartwychAgentow(){

    }

}
