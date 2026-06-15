package org.example;

// Zarządza porą dnia i widocznością na planszy (cykl dzień/noc + mgła)
public final class TimeOfDay {
    private static int durationDay = SimulationParameters.getInstance().getDayNightCycle()[0];
    private static int durationNight = SimulationParameters.getInstance().getDayNightCycle()[0];
    private static int durationTotal = durationDay + durationNight;
    private static double fogLevel = 1;  // Mnożnik widoczności (1 = brak mgły)

    private static double minVisibility = 0.5;
    private static double maxVisibility = 1.2;

    // Oblicza poziom widoczności na podstawie ticka (cykl dzień/noc + fala cosinus)
    public static double getVisibilityLevel(int currentTick) {
        int tickInCycle = currentTick % durationTotal;
        double peakTick = (durationDay - 1) / 2.0;

        double angle = 2 * Math.PI * (tickInCycle - peakTick) / durationTotal;
        double cosValue = Math.cos(angle);
        double normalizedValue = (cosValue + 1) / 2.0;
        double visibility = (minVisibility + (maxVisibility - minVisibility) * normalizedValue) * fogLevel;

        return Math.max(0.2, visibility);  // Minimum 20% widoczności
    }

    // Ustawia poziom mgły (przez zdarzenie Fog)
    public static void setFogLevel(double fog) {
        fogLevel = fog;
    }
}