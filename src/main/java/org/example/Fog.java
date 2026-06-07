package org.example;

public class Fog implements Event {
    private static final double[] fogIntensity = SimulationParameters.getInstance().getFogIntensity();

    public Fog(){}

    @Override
    public void trigger(Space[][] board) {
        double visibilityModifier = 1.0 - RNG.nextDouble(fogIntensity[0], fogIntensity[1]);
        TimeOfDay.setFogLevel(visibilityModifier);
    }
}
