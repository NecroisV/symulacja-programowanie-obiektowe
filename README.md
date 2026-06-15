# Symulator Epidemii Zombie 

Projekt stanowi zaawansowany, dwuwymiarowy symulator oparty na agentach (Agent-Based Simulation). Silnik symulacji modeluje interakcje pomiędzy dwiema głównymi frakcjami: Ocalałymi (Survivor) oraz Zakażonymi (Infected). Świat gry generowany jest proceduralnie za pomocą automatów komórkowych, co pozwala na tworzenie organicznych układów ścian, korytarzy oraz stref bezpiecznych.

Wszyscy agenci podejmują decyzje autonomicznie w oparciu o dynamiczne mapowanie wag przestrzennych (pól sił atrakcji i repulsji) w zasięgu wzroku (FOV), co eliminuje sztywne ścieżki i pozwala na wyłanianie się złożonych zachowań stadnych oraz taktycznych.

## 1. Kluczowe Funkcjonalności

### Modelowanie Agentów i Decyzyjność
* Ocalali (Survivor): Dążą do przetrwania poprzez poszukiwanie zasobów naturalnych, zbieranie wyposażenia (broń, odzież) oraz unikanie zakażonych. Posiadają wskaźnik energii – niedobór energii prowadzi do głodu (starvation) i stopniowej utraty zdrowia.
* Zakażeni (Infected): Agresywni agenci poszukujący ocalałych. Reagują na zapach/wzrok, a ich wagi ruchu są silnie modyfikowane przez obecność ludzi poza bezpiecznymi strefami.
* Mechanika Ran (Wound System): Walka lub zdarzenia losowe mogą skutkować trwałym lub tymczasowym upośledzeniem agenta. Wyróżniamy rany głowy (HeadWound – redukcja FOV), rany rąk (ArmWound – redukcja siły) oraz rany nóg (LegWound – redukcja prędkości).

### Ekonomia Świata i Przedmioty
* Wyposażenie (Equipment): Przedmioty generowane na mapie przy użyciu wzorca Factory. Broń (Weapon) zwiększa efektywną siłę bojową, tracąc przy tym trwałość. Ubiór (Clothes) redukuje otrzymywane obrażenia, zmniejsza zużycie energii i oferuje ochronę przed infekcją (infectionPrevention).
* Zasoby Naturalne (environmentalResource): Pola regenerujące zdrowie i energię ocalałych z wbudowanym mechanizmem czasu odnowienia (respawn).

### Środowisko i Zdarzenia Losowe
* Cykl Dobowy (Day/Night Cycle): Dynamiczna zmiana oświetlenia i widoczności oparta na funkcji cosinusoidalnej (klasa TimeOfDay), bezpośrednio wpływająca na zasięg percepcji agentów.
* Dynamiczne Katastrofy (Dynamic Events): Zarządzane przez EventManager zdarzenia pogodowe i tektoniczne:
  * Mgła (Fog): Drastycznie ogranicza widoczność.
  * Burza (Thunderstorm): Losowo niszczy ściany i zabija agentów rażeniem piorunów.
  * Trzęsienie ziemi (Earthquake): Destabilizuje strukturę mapy, niszcząc losowe ściany.
* Strefy Bezpieczeństwa (SafeZone): Schronienia blokujące walkę, w których ocalali mogą leczyć rany. Posiadają ograniczony czas życia oraz próg destrukcji (jeśli otaczające strefę ściany zostaną zniszczone, strefa ulega likwidacji).

## 2. Struktura Projektu i Architektura Klas

| Klasa / Interfejs | Rola w Systemie | Zastosowane Koncepcje / Wzorce |
| :--- | :--- | :--- |
| `Agent` | Abstrakcyjna klasa bazowa dla wszystkich jednostek mobilnych. | Polimorfizm, Encapsulation |
| `Survivor` / `Infected` | Konkretne implementacje logiki ocalałych oraz zombie. | Dziedziczenie, Maszyna Stanów (wagi) |
| `Space` | Reprezentacja pojedynczego kafelka planszy (graf siatkowy). | Struktury powiązane (Up, Down, Left, Right) |
| `SimulationParameters` | Zgrupowanie konfiguracji symulacji, współczynników wagowych i prawdopodobieństw. | Singleton Pattern |
| `EquipmentFactory` | Odpowiada za losową lub sparametryzowaną generację ekwipunku. | Factory Pattern |
| `Event` / `EventManager` | Abstrakcja katastrof środowiskowych oraz menedżer ich cyklu życia. | Strategy Pattern |
| `DataCollector` | Zbieranie metryk czasu rzeczywistego (średni wiek, zdrowie, interakcje). | Analityka danych, Data Aggregation |
| `Render` | Odpowiada za niskopoziomowe rysowanie stanu świata na komponencie JavaFX. | Grafika 2D (GraphicsContext) |

## 3. Mechanizm Podejmowania Decyzji o Ruchu

Ruch agenta realizowany jest w metodzie `makeMove` klasy `Agent`. Co turę agent analizuje otoczenie za pomocą funkcji `whatAgentSaw`, filtrując obiekty w swoim polu widzenia (zależnym od pory dnia i ran głowy). Następnie wywoływana jest metoda polimorficzna `getAgentWeights`:
1. Wszystkie kafelki w polu widzenia otrzymują początkową wagę bazową.
2. Wykrycie celów pożądanych (np. ocalały dla zakażonego, surowiec dla głodnego ocalałego) aplikuje wysokie wartości dodatnie na dany kafelek wraz z efektem rozlania wag (weight spill) na sąsiednie pola, tworząc gradient przyciągania.
3. Wykrycie zagrożeń (zakażony w pobliżu człowieka) aplikuje wartości ujemne (repulsja).
4. Agent wybiera sąsiednie pole o najwyższej skumulowanej wadze. Jeśli ruch zostanie wykonany, ocalały zużywa energię.

## 4. Wymagania i Uruchomienie

### Wymagania systemowe
* Java Development Kit (JDK) w wersji 25.
* Gradle w wersji 9.3.0.
* Biblioteka JavaFX skonfigurowana w środowisku uruchomieniowym.

### Kompilacja i Uruchomienie (Gradle)
Aby skompilować i uruchomić projekt przy użyciu Gradle, wykonaj poniższe polecenia w katalogu głównym projektu:

```bash
# Czyszczenie starych plików i budowanie projektu
./gradlew clean build

# Uruchomienie aplikacji GUI
./gradlew run
