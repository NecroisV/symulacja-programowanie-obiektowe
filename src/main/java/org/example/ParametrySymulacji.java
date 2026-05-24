package org.example;

import java.util.Map;

public class ParametrySymulacji {
    private int poczatkowaLiczbaOcalalych;
    private int poczatkowaLiczbaZakazonych;
    private float szansaNaRanePoWalce;
    private Map<String, Float> wagiRuchu;
    private int[] szansePoczatkoweNaEkwipunekIRany;

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
