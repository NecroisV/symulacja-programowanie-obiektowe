package simulation;

import java.util.Random;

/**
 * Klasa narzędziowa odpowiedzialna za generowanie liczb pseudolosowych w symulacji.
 * Implementuje uproszczony wzorzec Singleton (w formie globalnej klasy narzędziowej ze statycznym dostępem),
 * zapewniając determinizm i powtarzalność wyników symulacji dzięki możliwości zainicjalizowania
 * wewnętrznego generatora określonym ziarnem (seed).
 */
public final class RNG {

    /** Wewnętrzna instancja generatora liczb losowych biblioteki standardowej. */
    private static Random rng;

    /**
     * Prywatny konstruktor blokujący możliwość tworzenia instancji klasy narzędziowej.
     */
    private RNG() {}

    /**
     * Inicjalizuje generator liczb pseudolosowych zadanym ziarnem.
     * Wywołanie tej metody z tym samym ziarnem gwarantuje identyczny ciąg losowań
     * przy każdym uruchomieniu symulacji.
     *
     * @param seed Wartość ziarna (seed) dla generatora losowego.
     */
    public static void initRNG(int seed){
        rng = new Random(seed);
    }

    /**
     * Generuje losową liczbę całkowitą z przedziału prawostronnie otwartego [0, max).
     *
     * @param max Górna, nieprzekraczalna granica losowania (wykluczona).
     * @return Losowa liczba całkowita większa lub równa 0 i mniejsza niż max.
     * @throws IllegalStateException Jeśli generator nie został wcześniej zainicjalizowany metodą {@link #initRNG(int)}.
     */
    public static int nextInt(int max){
        if(rng == null){
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextInt(max);
    }

    /**
     * Generuje losową liczbę całkowitą z przedziału obustronnie domkniętego [min, max].
     *
     * @param min Dolna granica przedziału (włącznie).
     * @param max Górna granica przedziału (włącznie).
     * @return Losowa liczba całkowita z zakresu od min do max.
     * @throws IllegalStateException Jeśli generator nie został wcześniej zainicjalizowany metodą {@link #initRNG(int)}.
     */
    public static int nextInt(int min, int max) {
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextInt(max - min + 1) + min;
    }

    /**
     * Generuje losową liczbę zmiennoprzecinkową typu float z przedziału prawostronnie otwartego [0.0f, 1.0f).
     *
     * @return Losowa wartość float z zakresu od 0.0 (włącznie) do 1.0 (wyłącznie).
     * @throws IllegalStateException Jeśli generator nie został wcześniej zainicjalizowany metodą {@link #initRNG(int)}.
     */
    public static float nextFloat() {
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextFloat();
    }

    /**
     * Generuje losową liczbę zmiennoprzecinkową typu float z przedziału prawostronnie otwartego [0.0f, max).
     *
     * @param max Górna granica skali losowania (wykluczona).
     * @return Losowa wartość float z zakresu od 0.0 (włącznie) do wartości max (wyłącznie).
     * @throws IllegalStateException Jeśli generator nie został wcześniej zainicjalizowany metodą {@link #initRNG(int)}.
     */
    public static float nextFloat(int max){
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextFloat() * max;
    }

    /**
     * Generuje losową liczbę zmiennoprzecinkową typu float w oparciu o podany zakres.
     * Z uwagi na implementację wzoru, zakres wynosi w przybliżeniu [min, max + 1.0f).
     *
     * @param min Dolna wartość bazowa przedziału.
     * @param max Mnożnik skali modyfikujący rozpiętość przedziału.
     * @return Losowa wartość float ze skalowanego przedziału.
     * @throws IllegalStateException Jeśli generator nie został wcześniej zainicjalizowany metodą {@link #initRNG(int)}.
     */
    public static float nextFloat(float min, float max) {
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextFloat() * (max - min + 1) + min;
    }

    /**
     * Generuje losową liczbę zmiennoprzecinkową typu double z przedziału prawostronnie otwartego [0.0, 1.0).
     *
     * @return Losowa wartość double z zakresu od 0.0 (włącznie) do 1.0 (wyłącznie).
     * @throws IllegalStateException Jeśli generator nie został wcześniej zainicjalizowany metodą {@link #initRNG(int)}.
     */
    public static double nextDouble() {
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextDouble();
    }

    /**
     * Generuje losową liczbę zmiennoprzecinkową typu double z przedziału prawostronnie otwartego [0.0, max).
     *
     * @param max Górna granica skali losowania (wykluczona).
     * @return Losowa wartość double z zakresu od 0.0 (włącznie) do wartości max (wyłącznie).
     * @throws IllegalStateException Jeśli generator nie został wcześniej zainicjalizowany metodą {@link #initRNG(int)}.
     */
    public static double nextDouble(double max){
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextDouble() * max;
    }

    /**
     * Generuje losową liczbę zmiennoprzecinkową typu double w oparciu o podany zakres.
     * Z uwagi na implementację wzoru, zakres wynosi w przybliżeniu [min, max + 1.0).
     *
     * @param min Dolna wartość bazowa przedziału.
     * @param max Mnożnik skali modyfikujący rozpiętość przedziału.
     * @return Losowa wartość double ze skalowanego przedziału.
     * @throws IllegalStateException Jeśli generator nie został wcześniej zainicjalizowany metodą {@link #initRNG(int)}.
     */
    public static double nextDouble(double min, double max) {
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextDouble() * (max - min + 1) + min;
    }
}