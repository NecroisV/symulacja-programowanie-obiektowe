package agent;

import simulation.Space;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Klasa reprezentująca zainfekowanego agenta (np. zombie) w symulacji.
 * Głównym celem tego agenta jest lokalizowanie i polowanie na ocalałych ({@link Survivor}).
 * Jednocześnie agent stara się unikać zbędnego tłoczenia się z innymi zainfekowanymi
 * oraz kategorycznie unika wchodzenia do bezpiecznych stref (Safe Zones).
 */
public class Infected extends Agent {

    /**
     * Konstruktor tworzący nowego zainfekowanego agenta o określonych statystykach bazowych.
     *
     * @param given_x        Początkowa współrzędna X na planszy.
     * @param given_y        Początkowa współrzędna Y na planszy.
     * @param given_health   Początkowa liczba punktów zdrowia agenta.
     * @param given_strength Bazowa siła ataku agenta.
     * @param given_FOV      Zasięg pola widzenia (Field of View).
     * @param given_speed    Szybkość poruszania się agenta (liczba ruchów na turę).
     */
    public Infected(int given_x, int given_y, int given_health, int given_strength, int given_FOV, int given_speed) {
        super(given_x, given_y, given_health, given_strength, given_FOV, given_speed);
    }

    /**
     * Oblicza i modyfikuje wagi pól w lokalnym obszarze percepcji zainfekowanego agenta,
     * determinując jego preferencje ruchowe w bieżącej turze.
     * <p>
     * Logika działania wagowania dla zainfekowanego:
     * <ul>
     * <li>Pola z zauważonymi ocalałymi otrzymują bardzo wysoką wagę dodatnią (dążenie do celu).</li>
     * <li>Pola z innymi zainfekowanymi są drastycznie penalizowane ujemnymi wartościami (unikanie tłoku).</li>
     * <li>Pola należące do bezpiecznych stref są silnie penalizowane (strach/blokada przed wejściem).</li>
     * </ul>
     *
     * @param start         Pole startowe (bieżąca pozycja agenta), od którego obliczana jest percepcja.
     * @param baseWeights   Mapa konfiguracji zawierająca domyślne wartości wag dla różnych zdarzeń i obiektów.
     * @param weightDivisor Dzielnik wagi używany przy rozlewaniu (propagacji) wag na sąsiednie pola.
     */
    @Override
    public void getAgentWeights(Space start, Map<String, Integer> baseWeights, int weightDivisor) {
        List<Space> localArea = getLocalArea(start);

        ArrayList<ArrayList<Space>> thingsAgentSaw = this.whatAgentSaw(start);
        List<Space> seenAgents = thingsAgentSaw.get(2);

        // Reset wag w lokalnym obszarze
        for (Space space : localArea) {
            space.changeWeight(-space.getWeight());
            space.changeWeight(1);
        }

        for (Space space : localArea) {
            if (seenAgents.contains(space)) {
                for (Agent a : space.getAgents()) {
                    if (a instanceof Survivor && !space.isInSafeZone()) {
                        // Wysoka waga dla pól z widzianymi ocalałymi
                        addWeightWithSpill(space, baseWeights.getOrDefault("infectedCurrentSeenSurvivor", 100), weightDivisor);
                    } else if (a instanceof Infected && a != this) {
                        // Niska waga dla pól z innymi zakażonymi (unikają się)
                        addWeightWithSpill(space, baseWeights.getOrDefault("infectedCloseInfected", 5), weightDivisor);
                        space.changeWeight(-256128 - space.getWeight());
                    }
                }
            }
            // Unikanie stref bezpieczeństwa
            if (space.isInSafeZone()) {
                space.changeWeight(-1000 * Math.abs(space.getWeight()));
            }
        }
    }
}