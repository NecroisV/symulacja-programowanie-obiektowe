package agent;

/**
 * Abstrakcyjna klasa bazowa reprezentująca ranę (Wound) odniesioną przez agenta.
 * <p>
 * Stanowi architektoniczny fundament dla polimorficznych, wyspecjalizowanych typów urazów
 * (takich jak rany głowy, rąk czy nóg). Klasy pochodne implementują konkretne efekty
 * i modyfikatory negatywne, które upośledzają parametry biomechaniczne agentów – na przykład
 * ograniczenie pola widzenia (FOV), redukcję siły bojowej czy spadek prędkości poruszania się.
 * </p>
 * <p>
 * Rany są generowane dynamicznie na podstawie prawdopodobieństw określonych w konfiguracji
 * (np. po przegranej bitwie) i stanowią kluczowy czynnik motywujący ocalałych do odwiedzenia
 * stref bezpieczeństwa (SafeZones) w celu poddania się rekonwalescencji.
 * </p>
 */
public abstract class Wound {

}