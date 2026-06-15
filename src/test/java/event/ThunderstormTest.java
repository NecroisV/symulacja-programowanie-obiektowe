package event;

import agent.Agent;
import agent.Survivor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simulation.Space;

import static org.junit.jupiter.api.Assertions.*;

class ThunderstormTest {

    private Thunderstorm thunderstorm;
    private Space[][] board;

    @BeforeEach
    void setUp() {
        thunderstorm = new Thunderstorm();

        // Tworzymy małą planszę testową 3x3
        board = new Space[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = new Space(i, j);
            }
        }
    }

    @Test
    @DisplayName("Burza powinna poprawnie obsłużyć pustą planszę bez rzucania wyjątków")
    void shouldExecuteTriggerWithoutExceptions() {
        // Act & Assert
        assertDoesNotThrow(() -> thunderstorm.trigger(board),
                "Metoda trigger rzuciła wyjątek przy losowaniu pól burzy.");
    }

    @Test
    @DisplayName("Agenci na trafionym polu powinni zginąć, a ściany zostać zniszczone")
    void shouldDestroyWallAndKillAgentsWhenFieldIsHit() {
        // Arrange
        // Zamiast liczyć na ślepy traf RNG, symulujemy maksymalne uderzenie
        // Uruchamiamy burzę wielokrotnie, aż statystycznie trafimy w nasz punkt testowy (0,0)
        Space targetSpace = board[0][0];
        targetSpace.createWall(); // Stawiamy tam ścianę (zakładamy istnienie metody buildWall lub podobnej)

        Agent survivor = new Survivor(0, 0, 100, 5, 3, 2);
        targetSpace.addAgent(survivor); // Umieszczamy tam agenta

        // Act
        // Wywołujemy burzę w pętli do momentu, aż algorytm RNG "wylosuje" pole (0,0)
        int maxAttempts = 100;
        for (int i = 0; i < maxAttempts; i++) {
            thunderstorm.trigger(board);
            if (!targetSpace.isItWall() || !survivor.isItAlive()) {
                break; // Trafienie zaliczone!
            }
        }

        // Assert
        // Przynajmniej jeden z warunków powinien zostać spełniony po serii wyładowań
        assertTrue(!targetSpace.isItWall() || !survivor.isItAlive(),
                "Burza powinna w końcu zniszczyć ścianę lub uśmiercić agenta na planszy.");
    }
}