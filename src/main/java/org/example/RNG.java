package org.example;

import java.util.Random;

public final class RNG {
    private static Random rng;
    private RNG() {}

    public static void initRNG(int seed){
        rng = new Random(seed);
    }

    public static int nextInt(int max){
        if(rng == null){
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextInt(max);
    }
    public static int nextInt(int min, int max) {
        if (rng == null) {
            throw new IllegalStateException("RNG not initialized");
        }
        return rng.nextInt(max - min + 1) + min;
    }

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

// test deterministyczności
//    public static void main(String[] args){
//        initRNG(SimulationParameters.getInstance().getSimulationSeed());
//        int i = 0;
//        while (i < 10){
//            System.out.println(RNG.nextInt(0, 10));
//            System.out.println(RNG.nextFloat(0, 10));
//            System.out.println(RNG.nextDouble(0, 10));
//            i++;
//        }
//    }
}
