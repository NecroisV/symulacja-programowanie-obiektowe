package org.example;

// Menadżer zarządzający zdarzeniami losowymi (burza, mgła, trzęsienie)
public class EventManager {
    private static int currentDuration = 0;      // Pozostały czas trwania aktywnego zdarzenia
    private static int sinceLastEvent = 0;       // Tick od ostatniego zdarzenia
    private static int minimumCooldown = 10;     // Minimalna przerwa między zdarzeniami

    private static Event currentEvent = null;
    private static final double[] eventChances = SimulationParameters.getInstance().getEventChances();
    private static final double chanceToSpawn = eventChances[0];

    protected EventManager(){

    }

    // Sprawdza czy można wylosować nowe zdarzenie (cooldown + szansa)
    private static boolean canSpawnEvent(){
        sinceLastEvent++;
        if(sinceLastEvent >= minimumCooldown){
            double random = RNG.nextDouble();
            if(random <= chanceToSpawn){
                sinceLastEvent = 0;
                return true;
            }
        }
        return false;
    }

    // Próbuje wylosować nowe zdarzenie - typ zależy od wag w parametrach
    private static void trySpawnEvent(){
        if(canSpawnEvent()){
            double weightSum = eventChances[1]+eventChances[2]+eventChances[3];
            double roll = RNG.nextDouble(0, weightSum);
            double thresholdStorm = eventChances[1];
            double thresholdFog = eventChances[1]+eventChances[2];
            double thresholdEarthquake = eventChances[1]+eventChances[2]+eventChances[3];
            if (roll <= thresholdStorm){
                currentEvent = new Thunderstorm();
                currentDuration = RNG.nextInt(SimulationParameters.getInstance().getEventDuration()[0], SimulationParameters.getInstance().getEventDuration()[1]);
            }
            else if (roll <= thresholdFog){
                currentEvent = new Fog();
                currentDuration = RNG.nextInt(SimulationParameters.getInstance().getEventDuration()[0], SimulationParameters.getInstance().getEventDuration()[1]);
            }
            else if (roll <= thresholdEarthquake){
                currentEvent = new Earthquake();
                currentDuration = RNG.nextInt(SimulationParameters.getInstance().getEventDuration()[0], SimulationParameters.getInstance().getEventDuration()[1]);
            }
            else {
                System.err.println("Z jakiegos powodu zaden event nie zostal wylosowany");
                currentEvent = null;
            }
        }
    }

    // Główna metoda wywoływana co tick - zarządza aktywnym zdarzeniem
    public static void runEventCheck(Space[][] board){
        if(currentEvent == null){
            trySpawnEvent();  // Brak zdarzenia - próbuj wylosować nowe
        }
        else if(currentDuration > 0){
            currentEvent.trigger(board);  // Aktywne zdarzenie - wyzwól efekt
            currentDuration--;
            if(currentDuration == 0){
                TimeOfDay.setFogLevel(1);  // Zakończenie zdarzenia - reset mgły
                currentEvent = null;
            }
        }
    }

    public static Event getCurrentEvent(){
        return currentEvent;
    }
}