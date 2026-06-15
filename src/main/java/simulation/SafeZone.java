package simulation;

import agent.Survivor;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasa reprezentująca bezpieczną strefę (Safe Zone) na planszy symulacji.
 * Bezpieczna strefa zapewnia ocalałym ({@link Survivor}) schronienie przed zainfekowanymi,
 * blokuje możliwość prowadzenia walk oraz oferuje szansę na regenerację punktów zdrowia w każdym ticku.
 * <p>
 * Strefa posiada ograniczony cykl życia – może wygasnąć po upływie określonego czasu
 * lub ulec zniszczeniu, jeśli zbyt wysoki procent ścian tworzących schronienie zostanie zburzony.
 * </p>
 */
public class SafeZone {
    /** Flaga określająca, czy wewnątrz strefy obowiązuje całkowity zakaz walki. */
    private boolean fightingProhibited;

    /** Szansa (wartość z przedziału [0.0, 1.0]) na uleczenie rany agenta w pojedynczym ticku symulacji. */
    private float chanceToHeal;

    /** Procentowy próg zniszczenia (wartość z przedziału [0.0, 1.0]) ścian, po którego przekroczeniu strefa upada. */
    private float destructionThreshold;

    /** Całkowita liczba segmentów ścian znajdujących się w granicach tej strefy. */
    private int allWallsCount;

    /** Liczba ścian w strefie, które zostały dotychczas zniszczone. */
    private int destroyedWallsCount;

    /** Maksymalny czas życia strefy wyrażony w tickach symulacji. */
    private int lifespanTicks;

    /** Aktualny wiek strefy (liczba ticków, które upłynęły od jej utworzenia). */
    private int currentTick = 0;

    /** Lista wszystkich pól planszy ({@link Space}) wchodzących w skład tej strefy bezpieczeństwa. */
    private List<Space> coveredSpaces = new ArrayList<>();

    /**
     * Konstruktor tworzący nową strefę bezpieczeństwa.
     * Automatycznie losuje maksymalny czas trwania strefy w przedziale od 100 do 1500 ticków.
     *
     * @param chanceToHeal         Prawdopodobieństwo uleczenia agenta w każdym ticku.
     * @param destructionThreshold Ułamek zniszczonych ścian kwalifikujący strefę do likwidacji.
     */
    public SafeZone(float chanceToHeal, float destructionThreshold) {
        this.fightingProhibited = true;
        this.chanceToHeal = chanceToHeal;
        this.destructionThreshold = destructionThreshold;
        this.destroyedWallsCount = 0;
        this.lifespanTicks = RNG.nextInt(100, 1500);
    }

    /**
     * Rejestruje i dodaje pole siatki do obszaru tej strefy bezpieczeństwa.
     * Metoda automatycznie ustawia w przekazanym polu referencję zwrotną do tej strefy.
     *
     * @param space Obiekt pola {@link Space}, które ma zostać objęte strefą.
     */
    public void addSpace(Space space) {
        coveredSpaces.add(space);
        space.setSafeZone(this);
    }

    /**
     * Przeszukuje wszystkie przypisane pola strefy w celu zliczenia nienaruszonych ścian.
     * Wynik jest zapisywany w polu {@code allWallsCount} i służy jako baza do obliczania progu zniszczenia.
     */
    public void countWalls() {
        allWallsCount = 0;
        for (Space space : coveredSpaces) {
            if (space.isItWall()) {
                allWallsCount++;
            }
        }
    }

    /**
     * Próbuje zaaplikować efekt leczenia na ocalałym agencie znajdującym się w strefie.
     * Leczenie nie powiedzie się, jeśli strefa została uprzednio zniszczona lub jeśli
     * test losowy oparty na parametrze {@code chanceToHeal} zwróci wynik negatywny.
     *
     * @param survivor Obiekt ocalałego agenta ({@link Survivor}), który podlega próbie uleczenia.
     */
    public void healSurvivor(Survivor survivor) {
        if (isSafeZoneDestroyed()) return;
        if (RNG.nextFloat() < chanceToHeal) {
            survivor.changeHealthLevel(10);
        }
    }

    /**
     * Odnotowuje fakt zniszczenia pojedynczej ściany należącej do strefy.
     * Po inkrementacji licznika zniszczeń metoda sprawdza, czy przekroczono dopuszczalny próg destrukcji,
     * i jeśli tak – natychmiast inicjuje procedurę usunięcia strefy.
     */
    public void commitWallDestruction() {
        destroyedWallsCount++;
        if (isSafeZoneDestroyed()) {
            destroySafeZone();
        }
    }

    /**
     * Aktualizuje wiek strefy (inkrementuje licznik tur) oraz weryfikuje warunki jej zamknięcia.
     * Strefa zostaje zlikwidowana w przypadku przekroczenia czasu życia lub fizycznego zniszczenia barier.
     *
     * @return {@code true}, jeśli strefa wygasła/została zniszczona w tej turze i powinna zostać usunięta z menedżera;
     * {@code false}, jeśli strefa jest wciąż aktywna i stabilna.
     */
    public boolean updateAndCheckExpiry() {
        currentTick++;
        if (currentTick >= lifespanTicks) {
            destroySafeZone();
            return true;
        }
        if (isSafeZoneDestroyed()) {
            destroySafeZone();
            return true;
        }
        return false;
    }

    /**
     * Weryfikuje, czy stosunek zniszczonych ścian do wszystkich ścian w strefie
     * przekroczył zdefiniowany próg wartości {@code destructionThreshold}.
     *
     * @return {@code true}, jeśli procent zniszczeń kwalifikuje strefę do likwidacji;
     * {@code false}, jeśli strefa posiada wystarczającą strukturę ścian lub nie zawiera barier.
     */
    public boolean isSafeZoneDestroyed() {
        if (allWallsCount == 0) return false;
        return (float) destroyedWallsCount / allWallsCount >= destructionThreshold;
    }

    /**
     * Likwiduje strefę bezpieczeństwa.
     * Metoda iteruje po wszystkich powiązanych polach planszy, usuwając z nich referencje do tej strefy,
     * a na koniec czyści wewnętrzną listę pól schronienia.
     */
    public void destroySafeZone() {
        for (Space space : coveredSpaces) {
            space.setSafeZone(null);
        }
        coveredSpaces.clear();
    }

    /**
     * Sprawdza, czy w strefie zablokowana jest możliwość walki.
     *
     * @return {@code true}, jeśli walka jest zabroniona.
     */
    public boolean isFightingProhibited() {
        return fightingProhibited;
    }

    /**
     * Zwraca listę pól wchodzących w skład strefy.
     *
     * @return Lista obiektów {@link Space}.
     */
    public List<Space> getCoveredSpaces() {
        return coveredSpaces;
    }

    /**
     * Zwraca całkowitą liczbę początkowych ścian w strefie.
     *
     * @return Liczba ścian.
     */
    public int getAllWallsCount() {
        return allWallsCount;
    }

    /**
     * Zwraca liczbę zniszczonych ścian w obrębie strefy.
     *
     * @return Liczba zniszczonych ścian.
     */
    public int getDestroyedWallsCount() {
        return destroyedWallsCount;
    }

    /**
     * Zwraca maksymalny czas działania strefy.
     *
     * @return Czas w tickach.
     */
    public int getLifespanTicks() {
        return lifespanTicks;
    }

    /**
     * Zwraca aktualny wiek strefy (liczbę wykonanych ticków).
     *
     * @return Aktualny tick życia strefy.
     */
    public int getCurrentTick() {
        return currentTick;
    }
}