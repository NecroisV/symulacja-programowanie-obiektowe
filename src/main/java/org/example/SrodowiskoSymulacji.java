package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SrodowiskoSymulacji {
    private Pole[][] plansza;
    private List<Agent> listaAgentow = new ArrayList<>();
    private List<Agent> listaWyczerpanychAgentow = new ArrayList<>();
    private int aktualnyTick = 0;
    private float poziomOswietlenia;
    private PoraDnia aktualnaPoraDnia;
    private ParametrySymulacji parametry = new ParametrySymulacji();
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
        rozpatrzZdarzenieLosowe();
        wykonajRuch();
        wyswietlPlansze();
        rozpatrzInterakcje();
        //wyswietlPlansze();
        usunMartwychAgentow();
        //wyswietlPlansze();
        aktualizujPoreDnia();
    }

    private void aktualizujPoreDnia(){
        obliczPoziomOswietlenia();
        aktualnyTick++;
    }

    private void obliczPoziomOswietlenia(){
        poziomOswietlenia = 1.0f;
    }

    private void stworzPlansze(int szerokosc, int wysokosc){
        plansza = new Pole[wysokosc][szerokosc];
        Random random = new Random();
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
                if(random.nextInt(100)>=90) {plansza[y][x].stworzSciane();}
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

            if (!plansza[y][x].czyJestSciana()){
                Ocalaly ocalaly = new Ocalaly(x, y, zdrowie);
                listaAgentow.add(ocalaly);
                plansza[y][x].dodajAgneta(ocalaly);
                i ++;
            }
        }

        i = 0;
        while(i < liczbaZakazonych){
            int x = random.nextInt(szerokosc-1);
            int y = random.nextInt(wysokosc-1);

            if (!plansza[y][x].czyJestSciana()){
                Zakazony zakazony = new Zakazony(x, y, zdrowie);
                listaAgentow.add(zakazony);
                plansza[y][x].dodajAgneta(zakazony);
                i ++;
            }
        }
    }

    private void wykonajRuch(){
        int szerokosc = plansza[0].length;
        int wysokosc = plansza.length;

        listaWyczerpanychAgentow.clear();

        for(int i = 0; i < wysokosc; i++){
            for(int j = 0; j < szerokosc; j++){
                if(!plansza[i][j].czyJestSciana()){
                    List<Agent> oryginalniAgenci = plansza[i][j].przekazAgentow();

                    List<Agent> kopiaAgentow = new ArrayList<>(oryginalniAgenci);

                    for(Agent a : kopiaAgentow){
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
        int pierwotneY = pierwotnePole[0];
        int pierwotneX = pierwotnePole[1];

        int doceloweX = docelowePole[0];
        int doceloweY = docelowePole[1];
        plansza[doceloweY][doceloweX].dodajAgneta(a);

        plansza[pierwotneY][pierwotneX].usunAgenta(a);
    }

    private void rozpatrzInterakcje(){

    }

    private void transformujOcalalego(Ocalaly o, Zakazony z){

    }

    private void usunMartwychAgentow(){

    }

    private void rozpatrzZdarzenieLosowe(){

    }

    private void wyswietlPlansze() {
        int szerokosc = plansza[0].length;
        int wysokosc = plansza.length;

        //do czyszczenia podglądu
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println("\n--- TICK: " + aktualnyTick + " ---");
        for (int i = 0; i < wysokosc; i++) {
            for (int j = 0; j < szerokosc; j++) {
                Pole pole = plansza[i][j];
                if (pole.czyJestSciana()) {
                    System.out.print("# ");
                } else {
                    List<Agent> agenciNaPolu = pole.przekazAgentow();
                    if (agenciNaPolu.isEmpty()) {
                        System.out.print(". ");
                    } else if (agenciNaPolu.size() > 1) {
                        System.out.print(agenciNaPolu.size() + " ");
                    } else {
                        Agent a = agenciNaPolu.getFirst();
                        if (a instanceof Ocalaly) {
                            System.out.print("O ");
                        } else if (a instanceof Zakazony) {
                            System.out.print("Z ");
                        } else {
                            System.out.print("? ");
                        }
                    }
                }
            }
            System.out.println();
        }
    }

}
