package org.example;

import java.util.Map;

public class ParametrySymulacji {
    private int poczatkowaLiczbaOcalalych = 3;
    private int poczatkowaLiczbaZakazonych = 4;
    private float szansaNaRanePoWalce;
    private Map<String, Float> wagiRuchu;
    private int[] szansePoczatkoweNaEkwipunekIRany = new int[]{10, 15};

    public ParametrySymulacji(){

    }

    public void wczytajParametry(){

    }

    public int[] przekazParametryDoKreacjiAgentow(){
        return new int[]{poczatkowaLiczbaOcalalych, poczatkowaLiczbaZakazonych};
    }

    public int[] przekazSzansePoczatkoweNaEkwipunekIRany(){
        return szansePoczatkoweNaEkwipunekIRany;
    }
}
