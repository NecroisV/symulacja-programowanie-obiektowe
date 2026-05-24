package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SrodowiskoSymulacji {
    private Pole[][] plansza;
    private List<Agent> listaAgentow;
    private List<Agent> listaWyczerpanychAgentow;
    private int aktualnyTick;
    private float poziomOswietlenia;
    private PoraDnia aktualnaPoraDnia;
    private ParametrySymulacji parametry;
    private ZbieraczMetryk metryki;
    private List<StrefaBezpieczna> strefy = new ArrayList<>();
    private List<ZdarzenieLosowe> zdarzeniaLosowe = new ArrayList<>();

    public SrodowiskoSymulacji(int szerokosc, int wysokosc){
        stworzPlansze(szerokosc, wysokosc);
        int[] liczbyAgentow = parametry.przekazParametryDoKreacjiAgentow();
        int[] szanse = parametry.przekazSzansePoczatkoweNaEkwipunekIRany();
        stworzAgentow(liczbyAgentow[0], liczbyAgentow[1], szanse[0], szanse[1]);
    }

    public void krokSymulacji(){

    }

    public void aktualizujPoreDnia(){

    }

    public float obliczPoziomOswietlenia(){
        return 1.0f;
    }

    private void stworzPlansze(int szerokosc, int wysokosc){
        plansza = new Pole[wysokosc][szerokosc];
        for(int y = 0; y < wysokosc; y++){
            for(int x = 0; x < szerokosc; x++){
                plansza[y][x] = new Pole(x, y);
                if(x>0){
                    plansza[y][x].polaczLeft(plansza[y][x-1]);
                    plansza[y][x-1].polaczRight(plansza[y][x]);
                }
                if(x==szerokosc-1){
                    plansza[y][0].polaczLeft(plansza[y][x]);
                    plansza[y][x].polaczRight(plansza[y][0]);
                }
            }

            if(y>0){
                for(int x = 0; x < szerokosc; x++){
                    plansza[y][x].polaczUp(plansza[y-1][x]);
                    plansza[y-1][x].polaczDown(plansza[y][x]);
                }
            }
            if(y==wysokosc-1){
                for(int x = 0; x < szerokosc; x++){
                    plansza[0][x].polaczUp(plansza[y][x]);
                    plansza[y][x].polaczDown(plansza[0][x]);
                }
            }
        }
    }

    private void stworzAgentow(int liczbaOcalalych, int liczbaZakazonych, int sznasaNaEkwipunek, int szansaNaRane){
        int szerokosc = plansza[0].length;
        int wysokosc = plansza.length;

        Random random = new Random();

        int zdrowie = 100;

        int i = 0;
        while(i < liczbaOcalalych){
            int x = random.nextInt(szerokosc-1);
            int y = random.nextInt(wysokosc-1);

            if (!plansza[x][y].czyJestSciana()){
                Ocalaly ocalaly = new Ocalaly(x, y, zdrowie);
                listaAgentow.add(ocalaly);
                plansza[x][y].dodajAgneta(ocalaly);
                i ++;
            }
        }

        i = 0;
        while(i < liczbaZakazonych){
            int x = random.nextInt(szerokosc-1);
            int y = random.nextInt(wysokosc-1);

            if (!plansza[x][y].czyJestSciana()){
                Zakazony zakazony = new Zakazony(x, y, zdrowie);
                listaAgentow.add(zakazony);
                plansza[x][y].dodajAgneta(zakazony);
                i ++;
            }
        }
    }

    public void wykonajRuch(){
        int szerokosc = plansza[0].length;
        int wysokosc = plansza.length;
        for(int i = 0; i< wysokosc; i++){
            for(int j = 0; j < szerokosc; j++){
                if(!plansza[i][j].czyJestSciana()){
                    List<Agent> agenci = plansza[j][i].przekazAgentow();
                    for(Agent a:agenci){
                        if(!listaWyczerpanychAgentow.contains(a)) {
                            listaWyczerpanychAgentow.add(a);
                            int[] ruchAgenta = a.wykonajRuch(plansza[i][j]);
                            przemiescAgenta(new int[]{i, j}, ruchAgenta, a);
                        }
                    }
                }
            }
        }
    }

    private void przemiescAgenta(int[] pierwotnePole, int[] docelowePole, Agent a){
        plansza[docelowePole[0]][docelowePole[1]].dodajAgneta(a);
        plansza[pierwotnePole[0]][pierwotnePole[1]].usunAgenta(a);
    }

    public void rozpatrzInterakcje(){

    }

    public void transformujOcalalego(Ocalaly o, Zakazony z){

    }

    public void usunMartwychAgentow(){

    }

}
