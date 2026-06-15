package org.example;

import java.util.Random;

// Klasa narzędziowa do generowania liczb losowych (wzorzec Singleton)
public final class RNG {
    private static Random rng;
    private RNG() {}

    // Inicjalizuje generator z zadanym seedem (dla determinizmu)
    public static void initRNG(int seed){
        rng = new Random(seed);
    }

    // Losowa liczba całkowita [0, max)
    public static int nextInt(int max){
        if(rng == null){
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextInt(max);
    }

    // Losowa liczba całkowita [min, max]
    public static int nextInt(int min, int max) {
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextInt(max - min + 1) + min;
    }

    // Losowa liczba zmiennoprzecinkowa [0, 1)
    public static float nextFloat() {
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextFloat();
    }

    public static float nextFloat(int max){
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextFloat() * max;
    }

    public static float nextFloat(float min, float max) {
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextFloat() * (max - min + 1) + min;
    }

    // Losowa liczba podwójnej precyzji [0, 1)
    public static double nextDouble() {
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextDouble();
    }

    public static double nextDouble(double max){
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextDouble() * max;
    }

    public static double nextDouble(double min, double max) {
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextDouble() * (max - min + 1) + min;
    }
}