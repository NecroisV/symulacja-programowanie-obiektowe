package event;

import agent.Agent;
import simulation.RNG;
import simulation.Space;

/**
 * Klasa reprezentująca zdarzenie losowe typu Burza z piorunami (Thunderstorm).
 * Implementuje interfejs {@link Event}.
 * <p>
 * Burza jest gwałtowną anomalią pogodową o charakterze stricte destrukcyjnym.
 * Jej działanie polega na wygenerowaniu serii losowych uderzeń piorunów w kafelki planszy.
 * Każde trafienie skutkuje natychmiastowym, fizycznym zburzeniem struktury obronnej (ściany)
 * oraz permanentną śmiercią wszystkich agentów (zarówno ocalałych, jak i zakażonych)
 * znajdujących się w punkcie zero.
 * </p>
 */
public class Thunderstorm implements Event {

    /**
     * Domyślny konstruktor klasy wywoływany przez system zarządzania zdarzeniami (EventManager).
     */
    public Thunderstorm() {}

    /**
     * Inicjuje i wykonuje pełny cykl efektów burzy na przekazanej planszy symulacji.
     * <p>
     * <b>Algorytm działania:</b>
     * </p>
     * <ol>
     * <li>Dynamicznie kalkuluje maksymalną liczbę wyładowań (piorunów) za pomocą wzoru opierającego się
     * na proporcjach boków macierzy: {@code round((X_max + Y_max) / X_max)}. Wynik determinuje
     * górną granicę losowania dla klasy {@link RNG}.</li>
     * <li>Dla każdego uderzenia losowane są współrzędne dwuwymiarowe {@code (X, Y)} w granicach planszy.</li>
     * <li>Następuje weryfikacja pola docelowego {@link Space}:
     * <ul>
     * <li>Jeśli na polu znajduje się ściana, zostaje ona zniszczona za pomocą metody {@code destroyWall()},
     * co automatycznie powiadamia i osłabia przypisaną strefę bezpieczeństwa.</li>
     * <li>Wszystkie jednostki klasy {@link Agent} okupujące dany kafelek zostają uśmiercone metodą {@code die()}.</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param board Dwuwymiarowa macierz obiektów {@link Space} reprezentująca pełny obszar operacyjny symulacji.
     */
    @Override
    public void trigger(Space[][] board) {
        int amount = RNG.nextInt(Math.round((float) (board.length + board[0].length) / board.length));
        for(int i = 0; i < amount; i++) {
            int X = RNG.nextInt(board.length);
            int Y = RNG.nextInt(board[0].length);

            // Piorun niszczy ścianę na trafionym polu
            if (board[X][Y].isItWall()) {
                board[X][Y].destroyWall();
            }

            // Piorun zabija wszystkich agentów na trafionym polu
            for (Agent agent : board[X][Y].getAgents()) {
                agent.die();
            }
        }
    }
}