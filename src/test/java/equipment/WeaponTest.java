package equipment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeaponTest {

    @Test
    @DisplayName("Nowa broń powinna zadawać pełne, bazowe obrażenia")
    void shouldReturnMaxStrengthWhenDurabilityIsFull() {
        // Arrange
        Weapon weapon = new Weapon(20, 10, 10); // siła 20, wytrzymałość 10/10

        // Act & Assert
        assertEquals(20, weapon.calculateActualStrength(),
                "Broń o pełnej wytrzymałości musi zwracać baseStrength.");
    }

    @Test
    @DisplayName("Metoda loseDurability powinna zmniejszać sprawność broni o 1")
    void shouldDecreaseDurabilityByOne() {
        // Arrange
        Weapon weapon = new Weapon(10, 5, 5);

        // Act
        weapon.loseDurability();

        // Assert
        assertEquals(4, weapon.getActualDurability(), "Wytrzymałość powinna spaść z 5 do 4.");
    }

    @Test
    @DisplayName("Wytrzymałość nie może spaść poniżej 0 przy wielokrotnym użyciu")
    void shouldNotAllowDurabilityToGoBelowZero() {
        // Arrange
        Weapon weapon = new Weapon(10, 1, 5);

        // Act
        weapon.loseDurability(); // Spadek do 0
        weapon.loseDurability(); // Próba zejścia poniżej 0

        // Assert
        assertEquals(0, weapon.getActualDurability(), "Wytrzymałość musi zatrzymać się na 0.");
        assertEquals(0, weapon.calculateActualStrength(), "Zniszczona broń musi mieć 0 siły.");
    }

    @Test
    @DisplayName("Setter actualDurability powinien automatycznie przycinać wartości do dozwolonych granic")
    void shouldClampDurabilityInSetter() {
        // Arrange
        Weapon weapon = new Weapon(15, 5, 10);

        // Act & Assert
        weapon.setActualDurability(100); // Za dużo
        assertEquals(10, weapon.getActualDurability(), "Wartość powinna zostać obcięta do maxDurability (10).");

        weapon.setActualDurability(-50); // Za mało
        assertEquals(0, weapon.getActualDurability(), "Wartość powinna zostać podciągnięta do zera.");
    }
}