package org.example;

// Interfejs strategii rozmieszczania ekwipunku na planszy
public interface EquipmentSpawnStrategy {
    // Rozmieszcza zadaną liczbę broni i ubrań na planszy
    void spawnEquipment(Space[][] board, int weaponCount, int clothesCount);
}