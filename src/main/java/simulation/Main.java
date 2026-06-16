package simulation;

/**
 * Główna klasa startowa (punkt wejścia) aplikacji symulacyjnej.
 * Odpowiada za wstępną konfigurację parametrów globalnych, zainicjalizowanie
 * generatora liczb losowych (RNG) stałym ziarnem (seed) oraz uruchomienie
 * interfejsu graficznego opartego na technologii JavaFX.
 */
public class Main {

    /**
     * Główna metoda uruchomieniowa programu.
     * Ustawia stałe ziarno dla powtarzalności wyników, inicjalizuje klasę {@link RNG},
     * aktywuje dedykowany profil konfiguracji przygotowany pod pokaz/prezentację
     * oraz przekazuje kontrolę do właściwej aplikacji okienkowej {@link SimulationApp}.
     *
     * @param args Argumenty wiersza poleceń przekazywane do aplikacji podczas jej uruchamiania.
     */
    static void main(String[] args) {
        SimulationParameters.getInstance().setSeed(256);
        RNG.initRNG(SimulationParameters.getInstance().getSimulationSeed());
        SimulationParameters.setProfile(1); // ustawienie profilu pod pokaz
        SimulationApp.main(args);
    }
}