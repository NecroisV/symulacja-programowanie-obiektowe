package agent;

import equipment.Clothes;
import equipment.Equipment;
import equipment.Weapon;
import simulation.SimulationParameters;
import simulation.Space;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.round;

/**
 * Klasa reprezentująca agenta typu Ocalały (Survivor) w środowisku symulacji.
 * <p>
 * Ocalały jest kluczowym aktorem symulacji, którego głównym celem jest przetrwanie.
 * Posiada on zaawansowane mechaniki wewnętrzne, takie jak gospodarka energetyczna
 * (poziom energii i stan głodu), ograniczony inwentarz na broń i odzież ochronną,
 * zdolność do zbierania zasobów, wchodzenia w interakcje z innymi ludźmi (w tym agresywne
 * okradanie) oraz permanentną podatność na infekcję, skutkującą mutacją w zakażonego.
 * </p>
 * <p>
 * Jego zachowanie jest determinowane przez zaawansowany algorytm heurystyczny, który
 * dynamicznie dostosowuje wagi pól mapy w oparciu o bieżący stan biologiczny (głód, siła)
 * oraz obiekty dostrzeżone w polu widzenia (FOV).
 * </p>
 */
public class Survivor extends Agent {

    /** Bieżący poziom energii agenta ([0 - 100]). Spadek do zera aktywuje stan głodu. */
    private int energyLevel = 100;

    /** Maksymalny możliwy do osiągnięcia poziom energii (górny pułap regeneracji). */
    private int maxEnergyLevel = energyLevel;

    /** Maksymalna liczba slotów na przedmioty klasy {@link Weapon} (broń). */
    private int weaponCapacity = 2;

    /** Maksymalna liczba slotów na przedmioty klasy {@link Clothes} (odzież ochronna). */
    private int clothesCapacity = 2;

    /** Ekwipunek (inwentarz) agenta przechowujący zebrane przedmioty. */
    private List<Equipment> equipment = new ArrayList<>();

    /** Flaga określająca, czy agent cierpi z powodu skrajnego wyczerpania/głodu. */
    private boolean isStarving = false;

    /**
     * Konstruktor tworzący agenta ocalałego o określonych cechach biomechanicznych.
     *
     * @param given_x        Początkowa pozycja na osi X.
     * @param given_y        Początkowa pozycja na osi Y.
     * @param given_health   Maksymalne i początkowe punkty zdrowia.
     * @param given_strength Bazowa siła fizyczna (wpływa na walkę i redukcję strachu).
     * @param given_FOV      Zasięg pola widzenia (liczba kafelków).
     * @param given_speed    Prędkość ruchu (liczba pól na tick).
     */
    public Survivor(int given_x, int given_y, int given_health, int given_strength, int given_FOV, int given_speed) {
        super(given_x, given_y, given_health, given_strength, given_FOV, given_speed);
    }

    /**
     * Modyfikuje bieżący poziom energii agenta o zadaną wartość.
     * Dbając o zachowanie granic, metoda pilnuje, aby poziom nie przekroczył {@code maxEnergyLevel}.
     * W przypadku spadku energii poniżej 0, poziom jest zatrzaskiwany na wartości minimalnej,
     * a agent zostaje oznaczony jako głodujący, co natychmiast uruchamia procedurę {@link #starve()}.
     *
     * @param amount Wartość zmiany energii (dodatnia w przypadku konsumpcji, ujemna przy wysiłku).
     */
    public void changeEnergyLevel(int amount) {
        energyLevel += amount;
        if (energyLevel > maxEnergyLevel) energyLevel = maxEnergyLevel;
        if (energyLevel < 0){
            energyLevel = 0;
            isStarving = true;
            starve();
        }
    }

    /**
     * Mechanizm destrukcyjnego wpływu głodu na organizm.
     * Jeśli flaga starvation jest aktywna, agent traci **2 punkty zdrowia** (HP) w danym cyklu.
     */
    public void starve() {
        if(isStarving){
            this.changeHealthLevel(-2);
        }
    }

    /**
     * Sprawdza, czy agent aktualnie znajduje się w stanie permanentnego głodu.
     *
     * @return {@code true} jeśli poziom energii spadł do zera i agent odnosi obrażenia.
     */
    public boolean isStarving() {
        return isStarving;
    }

    /**
     * Wykonuje agresywną akcję grabieży na innym ocalałym (ofiara).
     * <p>
     * Metoda transferuje energię ofiary na rzecz napastnika (z zachowaniem bezpiecznego bufora)
     * oraz dokonuje migracji przedmiotów. Przedmioty ofiary są kolejno sprawdzane pod kątem
     * wolnego miejsca w inwentarzu agresora – te, które się zmieszczą, zostają zrabowane.
     * Niezależnie od wolnego miejsca agresora, inwentarz ofiary zostaje doszczętnie opróżniony.
     * </p>
     *
     * @param loser Agent ocalały, który przegrał starcie i zostaje okradziony.
     */
    public void steal(Survivor loser) {
        int stolenEnergy = loser.getEnergyLevel() - 10;
        this.changeEnergyLevel(Math.max(stolenEnergy, 10));
        loser.changeEnergyLevel(-Math.max(stolenEnergy, 10));

        List<Equipment> loserItems = new ArrayList<>(loser.equipment);

        for (Equipment item : loserItems) {
            if (this.hasSpaceInInventory(item)) {
                this.equipment.add(item);
            }
        }

        loser.equipment.clear();
    }

    /**
     * Zwraca pełną listę przedmiotów znajdujących się w inwentarzu ocalałego.
     *
     * @return Lista obiektów typu {@link Equipment}.
     */
    public List<Equipment> getEquipment() {
        return this.equipment;
    }

    /**
     * Inicjuje proces mutacji ocalałego w jednostkę zakażoną (Infected).
     * Tworzy nowego agenta frakcji zombie na dokładnie tych samych współrzędnych,
     * kalkulując jego zdrowie startowe jako połowę wartości zdrowia przekazanego
     * prototypu zainfekowanego. Pozostałe statystyki (siła, FOV, prędkość) są dziedziczone bezpośrednio.
     *
     * @param i Prototypowy obiekt klasy {@link Infected} służący za wzorzec mutacji szczepu.
     * @return Nowa instancja klasy {@link Infected}, gotowa do podstawienia na mapę.
     */
    public Infected transformIntoInfected(Infected i) {
        int[] position = getPosition();
        return new Infected(position[0], position[1], (int) round(i.getHealth() / 2.0), i.calculateStrength(), i.calculateFOV(), i.calculateSpeed());
    }

    /**
     * Generyczna metoda filtrująca zawartość inwentarza pod kątem konkretnego typu wyposażenia.
     * Wykorzystuje mechanizm Java Streams oraz refleksję do bezpiecznego odsiania i zrzutowania obiektów.
     *
     * @param <T>  Typ rozszerzający klasę {@link Equipment}.
     * @param type Klasa docelowa (np. {@code Weapon.class} lub {@code Clothes.class}).
     * @return Lista przedmiotów należących wyłącznie do żądanego typu.
     */
    public <T extends Equipment> List<T> getEquipmentOfType(Class<T> type) {
        return equipment.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Próbuje podnieść przedmiot leżący na ziemi na wskazanym polu.
     * <p>
     * Metoda weryfikuje limity pojemności inwentarza ocalałego dla konkretnego typu przedmiotu
     * (broń vs odzież). Jeżeli limit dla danego typu został osiągnięty, przedmiot zostaje
     * zwrócony z powrotem na ziemię (na kafelek {@code space}), a akcja kończy się niepowodzeniem.
     * </p>
     *
     * @param space Pole planszy {@link Space}, z którego podejmowana jest próba podniesienia przedmiotu.
     * @return {@code true} jeśli przedmiot pomyślnie trafił do ekwipunku; {@code false} w przeciwnym razie.
     */
    public boolean pickUpEquipment(Space space) {
        Equipment item = space.pickUpEquipment();
        if (item == null) return false;

        if (item instanceof Weapon && getEquipmentOfType(Weapon.class).size() >= weaponCapacity) {
            space.addEquipment(item);
            return false;
        }
        if (item instanceof Clothes && getEquipmentOfType(Clothes.class).size() >= clothesCapacity) {
            space.addEquipment(item);
            return false;
        }

        equipment.add(item);
        return true;
    }

    /**
     * Wymusza bezpośrednie dodanie przedmiotu do inwentarza agenta.
     * Stosowane głównie na etapie konfiguracji początkowej lub inicjalizacji scenariusza mapy.
     *
     * @param item Przedmiot dopisywany do inwentarza agenta.
     */
    public void getEquipment(Equipment item){
        if(item instanceof Weapon && getEquipmentOfType(Weapon.class).size() >= weaponCapacity){
            equipment.add(item);
        }
        else if (item instanceof Clothes && getEquipmentOfType(Clothes.class).size() >= clothesCapacity) {
            equipment.add(item);
        }
    }

    /**
     * Weryfikuje dostępność wolnego miejsca (slotu) w inwentarzu dla wybranego typu przedmiotu.
     *
     * @param item Obiekt przedmiotu, dla którego sprawdzany jest wolny slot.
     * @return {@code true} jeśli agent posiada wolne miejsce na ten typ wyposażenia; {@code false} jeśli osiągnięto limit.
     */
    public boolean hasSpaceInInventory(Equipment item) {
        if (item instanceof Weapon) {
            return getEquipmentOfType(Weapon.class).size() < weaponCapacity;
        }
        if (item instanceof Clothes) {
            return getEquipmentOfType(Clothes.class).size() < clothesCapacity;
        }
        return false;
    }

    /**
     * Zwraca aktualny poziom energii agenta.
     *
     * @return Wartość energii z zakresu [0 - 100].
     */
    public int getEnergyLevel() {
        return energyLevel;
    }

    /**
     * Nadpisuje bazową metodę wyznaczania wag atrakcyjności kafelków w lokalnym obszarze agenta.
     * <p>
     * Algorytm najpierw resetuje wagi w zasięgu wzroku ocalałego, a następnie aplikuje wektory
     * heurystyczne na podstawie skanowania otoczenia (obiekty dostrzeżone przez funkcję {@code whatAgentSaw}):
     * </p>
     * <ul>
     * <li><b>Strefy Bezpieczeństwa (SafeZones):</b> Silna atrakcyjność (waga dodatnia), motywująca do ucieczki do schronu, jeśli agent jest na otwartym terenie.</li>
     * <li><b>Zasoby Środowiskowe:</b> Atrakcyjność moderowana potrzebą przetrwania.</li>
     * <li><b>Ekwipunek na ziemi:</b> Atrakcyjność brana pod uwagę wyłącznie, gdy agent ma fizyczne miejsce w inwentarzu.</li>
     * <li><b>Zakażeni (Infected):</b> Silny czynnik repulsyjny (waga ujemna – strach). Wartość strachu jest dynamicznie łagodzona (zmniejszana) proporcjonalnie do poziomu siły fizycznej agenta (pewność siebie w walce).</li>
     * <li><b>Inni Ocaleni:</b> Nieufność lub niechęć (waga ujemna). Jeśli poziom energii agenta drastycznie spada (**poniżej 40**), repulsja pogłębia się, symulując unikanie potencjalnych rywali do surowców lub drapieżne nastawienie.</li>
     * </ul>
     *
     * @param start         Węzeł startowy (kafelek, na którym stoi agent).
     * @param baseWeights   Słownik bazowych wag wstrzyknięty z {@link SimulationParameters}.
     * @param weightDivisor Dystansowy dzielnik tłumienia wag (stosowany przy propagacji falowej "spill").
     */
    @Override
    public void getAgentWeights(Space start, Map<String, Integer> baseWeights, int weightDivisor) {
        int currentEnergy = getEnergyLevel();
        int strength = calculateStrength();

        List<Space> localArea = getLocalArea(start);

        ArrayList<ArrayList<Space>> thingsAgentSaw = this.whatAgentSaw(start);
        List<Space> seenResources = thingsAgentSaw.get(0);
        List<Space> seenAgents = thingsAgentSaw.get(1);
        List<Space> seenEquipment = thingsAgentSaw.get(2);

        // Reset wag w lokalnym obszarze
        for (Space space : localArea) {
            space.changeWeight(-space.getWeight());
            space.changeWeight(1);
        }

        for (Space space : localArea) {
            // Preferowanie stref bezpieczeństwa (jeśli nie jest się w jednej)
            if (space.isInSafeZone() && !start.isInSafeZone()) {
                addWeightWithSpill(space, baseWeights.getOrDefault("survivorSafeZone", 80), weightDivisor);
            }

            // Preferowanie zasobów
            if (seenResources.contains(space)) {
                addWeightWithSpill(space, baseWeights.getOrDefault("survivorResource", 30), weightDivisor);
            }

            // Preferowanie ekwipunku (tylko jeśli jest miejsce)
            if (seenEquipment.contains(space)) {
                for (Equipment equipment : space.getEquipmentOnGround()) {
                    if (this.hasSpaceInInventory(equipment)) {
                        addWeightWithSpill(space, baseWeights.getOrDefault("survivorEquipment", 50), weightDivisor);
                    }
                }
            }

            // Reakcja na widzianych agentów
            if (seenAgents.contains(space)) {
                for (Agent a : space.getAgents()) {
                    if (a instanceof Infected) {
                        // Unikanie zakażonych (strach zmniejszany przez siłę)
                        int baseFear = baseWeights.getOrDefault("survivorInfected", -100);
                        int fearReduction = strength * 10;
                        int finalInfectedWeight = Math.min(0, baseFear + fearReduction);
                        addWeightWithSpill(space, finalInfectedWeight, weightDivisor);
                    } else if (a instanceof Survivor && a != this) {
                        // Unikanie innych ocalałych (szczególnie gdy niska energia)
                        int baseSurvivorWeight = baseWeights.getOrDefault("survivorSurvivor", -20);
                        if (currentEnergy < 40) {
                            baseSurvivorWeight += (currentEnergy) * 2;
                        }
                        addWeightWithSpill(space, baseSurvivorWeight, weightDivisor);
                    }
                }
            }
        }
    }
}