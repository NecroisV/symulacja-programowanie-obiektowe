package agent;

import equipment.Clothes;
import equipment.Equipment;
import equipment.EquipmentFactory;
import equipment.Weapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simulation.Space;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SurvivorTest {

    private Survivor attacker;
    private Survivor victim;

    @BeforeEach
    void setUp() {
        // Inicjalizacja dwóch agentów na potrzebę testów interakcji
        // Parametry konstruktora: x, y, health, strength, FOV, speed
        attacker = new Survivor(0, 0, 100, 15, 3, 2);
        victim = new Survivor(1, 1, 100, 5, 3, 2);
    }

    // ==========================================
    // 1. TESTY GOSPODARKI ENERGETYCZNEJ I GŁODU
    // ==========================================

    @Test
    @DisplayName("changeEnergyLevel nie powinien pozwolić na przekroczenie maxEnergyLevel")
    void shouldClampEnergyToMaximum() {
        // Act
        attacker.changeEnergyLevel(50); // Próba dodania energii do początkowych 100

        // Assert
        assertEquals(100, attacker.getEnergyLevel(), "Energia powinna zostać przycięta do maxEnergyLevel (100).");
    }

    @Test
    @DisplayName("Spadek energii poniżej zera powinien aktywować głód i natychmiast zadać obrażenia")
    void shouldTriggerStarvationAndDealDamageWhenEnergyDropsBelowZero() {
        // Act
        attacker.changeEnergyLevel(-150); // Drastyczny spadek poniżej 0

        // Assert
        assertEquals(0, attacker.getEnergyLevel(), "Energia powinna zatrzasnąć się na wartości minimalnej (0).");
        assertTrue(attacker.isStarving(), "Flaga isStarving powinna zmienić się na true.");
        assertEquals(98, attacker.getHealth(), "Agent powinien stracić dokładnie 2 punkty HP przy automatycznym wywołaniu starve().");
    }

    // ==========================================
    // 2. TESTY MECHANIKI GRABIEŻY (STEAL)
    // ==========================================

    @Test
    @DisplayName("Metoda steal powinna przenieść energię z ofiary na napastnika i wyczyścić inwentarz ofiary")
    void shouldTransferEnergyAndLootItemsDuringSteal() {
        // Arrange
        victim.changeEnergyLevel(-20); // Ofiara ma teraz 80 energii
        Weapon victimWeapon = new Weapon(10, 5, 5);
        victim.getEquipment().add(victimWeapon); // Ręcznie wstrzykujemy broń do inwentarza ofiary

        // Act
        attacker.steal(victim);

        // Assert
        // Wyliczenie energii: stolenEnergy = 80 - 10 = 70.
        // Ofiara traci: 80 - 70 = 10.
        assertEquals(10, victim.getEnergyLevel(), "Ofiara powinna stracić skradzioną energię (powinno jej zostać 10).");
        assertEquals(1, attacker.getEquipment().size(), "Napastnik powinien pomyślnie zrabować broń.");
        assertTrue(victim.getEquipment().isEmpty(), "Inwentarz ofiary musi zostać doszczętnie opróżniony.");
    }

    // ==========================================
    // 3. TESTY INTERFEJSU INWENTARZA (STREAMS & REFLEKSJA)
    // ==========================================

    @Test
    @DisplayName("getEquipmentOfType powinien filtrować i zwracać wyłącznie przedmioty wybranego typu")
    void shouldFilterEquipmentByType() {
        // Arrange
        attacker.getEquipment().add(new Weapon(10, 5, 5));
        // Używamy fabryki, ponieważ Clothes nie ma bezargumentowego konstruktora
        attacker.getEquipment().add(EquipmentFactory.createClothes(2, 1, 5));
        attacker.getEquipment().add(new Weapon(20, 5, 5));

        // Act
        List<Weapon> weapons = attacker.getEquipmentOfType(Weapon.class);
        List<Clothes> clothes = attacker.getEquipmentOfType(Clothes.class);

        // Assert
        assertEquals(2, weapons.size(), "Powinien odfiltrować dokładnie 2 bronie.");
        assertEquals(1, clothes.size(), "Powinien odfiltrować dokładnie 1 sztukę odzieży.");
    }

    // ==========================================
    // 4. TESTY PODNOSZENIA PRZEDMIOTÓW (PICK UP)
    // ==========================================

    @Test
    @DisplayName("pickUpEquipment nie powinien pozwolić na podniesienie broni, jeśli osiągnięto weaponCapacity")
    void shouldRejectPickingUpWeaponWhenInventoryIsFull() {
        // Arrange
        // Zapychamy inwentarz dwiema broniami (osiągamy weaponCapacity = 2)
        attacker.getEquipment().add(new Weapon(5, 5, 5));
        attacker.getEquipment().add(new Weapon(5, 5, 5));

        // Tworzymy pole, na którym leży trzecia broń
        Space space = new Space(0, 0);
        Weapon groundWeapon = new Weapon(10, 10, 10);
        space.addEquipment(groundWeapon);

        // Act
        boolean result = attacker.pickUpEquipment(space);

        // Assert
        assertFalse(result, "Próba podniesienia powinna zwrócić false z powodu braku wolnego miejsca.");
        assertEquals(2, attacker.getEquipmentOfType(Weapon.class).size(), "W ekwipunku powinny pozostać maksymalnie 2 bronie.");
    }

    // ==========================================
    // 5. TESTY MUTACJI W ZAKAŻONEGO
    // ==========================================

    @Test
    @DisplayName("transformIntoInfected powinien stworzyć zakażonego o połowie HP prototypu na tych samych współrzędnych")
    void shouldTransformIntoInfectedWithCorrectStats() {
        // Arrange
        // Przenosimy napastnika na losową pozycję startową (np. ustawiając ją w polach nadrzędnych klasy Agent)
        // Tworzymy bazowy prototyp zombie, który posłuży za wzorzec mutacji szczepu
        Infected prototype = new Infected(0, 0, 100, 20, 4, 3);

        // Act
        Infected mutatedZombie = attacker.transformIntoInfected(prototype);

        // Assert
        assertNotNull(mutatedZombie, "Zmutowany zombie nie może być obiektem typu null.");
        assertEquals(50, mutatedZombie.getHealth(), "Zdrowie zmutowanego powinno wynosić dokładnie połowę zdrowia prototypu (100 / 2 = 50).");
        assertEquals(attacker.getPosition()[0], mutatedZombie.getPosition()[0], "Współrzędna X zakażonego musi odpowiadać pozycji ocalałego.");
        assertEquals(attacker.getPosition()[1], mutatedZombie.getPosition()[1], "Współrzędna Y zakażonego musi odpowiadać pozycji ocalałego.");
    }
}