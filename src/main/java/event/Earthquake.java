package event;

import simulation.RNG;
import simulation.SimulationParameters;
import simulation.Space;

/**
 * Klasa reprezentująca zdarzenie losowe w postaci trzęsienia ziemi.
 * Jest to jedno ze zdarzeń implementujących interfejs {@link Event}.
 * Efektem trzęsienia ziemi jest bezpowrotne niszczenie losowych segmentów ścian na planszy symulacji.
 */
public class Earthquake implements Event {

    /** * Prawdopodobieństwo zniszczenia pojedynczego segmentu ściany podczas trzęsienia ziemi.
     * Wartość pobierana jest z globalnych parametrów symulacji.
     */
    private static double wallDestroyChance = SimulationParameters.getInstance().getEarthquakeWallDestroyChance();

    /**
     * Domyślny konstruktor inicjalizujący zdarzenie trzęsienia ziemi.
     */
    public Earthquake(){}

    /**
     * Wyzwala efekt trzęsienia ziemi na całej planszy symulacji.
     * Metoda iteruje po wszystkich polach siatki i w przypadku napotkania ściany,
     * dokonuje losowania determinującego jej ewentualne zniszczenie.
     *
     * @param board Dwuwymiarowa tablica obiektów {@link Space} reprezentująca aktualną planszę symulacji.
     */
    @Override
    public void trigger(Space[][] board) {
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[i].length; j++){
                if(board[i][j].isItWall()){
                    if(RNG.nextFloat() <= wallDestroyChance) {
                        board[i][j].destroyWall();
                    }
                }
            }
        }
    }
}