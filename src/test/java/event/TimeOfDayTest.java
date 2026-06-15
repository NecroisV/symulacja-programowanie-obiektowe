package event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TimeOfDayTest {

    @Test
    @DisplayName("Poziom widoczności nigdy nie powinien spaść poniżej twardego limitu 20%")
    void shouldNotDropVisibilityBelowTwentyPercent() {
        // Act & Assert
        // Testujemy duży zakres ticków (np. 1000 kroków), aby upewnić się, że żadna faza nocy nie złamie limitu
        for (int tick = 0; tick < 1000; tick++) {
            double visibility = TimeOfDay.getVisibilityLevel(tick);
            assertTrue(visibility >= 0.2, "Widoczność spadła poniżej 0.2 na ticku: " + tick);
        }
    }

    @Test
    @DisplayName("Gęsta mgła powinna drastycznie ograniczyć globalną widoczność")
    void shouldReduceVisibilityWhenFogIsActive() {
        // Arrange
        TimeOfDay.setFogLevel(1.0); // Reset do pełnej widoczności
        double normalVisibility = TimeOfDay.getVisibilityLevel(10); // Pobieramy bazową widoczność dla ticka 10

        // Act
        TimeOfDay.setFogLevel(0.4); // Nakładamy silną mgłę (redukcja o 60%)
        double fogVisibility = TimeOfDay.getVisibilityLevel(10);

        // Assert
        assertTrue(fogVisibility < normalVisibility, "Mgła powinna zmniejszyć widoczność w porównaniu do normalnych warunków.");

        // Czyszczenie stanu po teście (dobra praktyka przy polach static!)
        TimeOfDay.setFogLevel(1.0);
    }
}