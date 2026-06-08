package org.example;

public final class TimeOfDay {
    private static int durationDay = SimulationParameters.getInstance().getDayNightCycle()[0];
    private static int durationNight = SimulationParameters.getInstance().getDayNightCycle()[0];
    private static int durationTotal = durationDay + durationNight;
    private static double fogLevel = 1;

    private static double minVisibility = 0.5;
    private static double maxVisibility = 1.2;

    public static double getVisibilityLevel(int currentTick) {
        int tickInCycle = currentTick % durationTotal;
        double peakTick = (durationDay - 1) / 2.0;

        double angle = 2 * Math.PI * (tickInCycle - peakTick) / durationTotal;
        double cosValue = Math.cos(angle);
        double normalizedValue = (cosValue + 1) / 2.0;
        double visibility = (minVisibility + (maxVisibility - minVisibility) * normalizedValue) * fogLevel;

        return Math.max(0.2, visibility);
    }

    public static void setFogLevel(double fog) {
        fogLevel = fog;
    }

}
