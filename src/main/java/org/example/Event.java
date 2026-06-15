package org.example;

// Interfejs dla wszystkich zdarzeń losowych w symulacji
public interface Event {
    // Wyzwala efekt zdarzenia na planszy
    void trigger(Space[][] board);
}