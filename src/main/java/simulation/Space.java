package simulation;

import agent.Agent;
import equipment.Equipment;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasa reprezentująca pojedynczy kafelek (węzeł) na dwuwymiarowej planszy symulacji.
 * Działa jako kontener przechowujący informacje o stanie kafelka (czy jest ścianą),
 * obecnych na nim agentach, porzuconym ekwipunku oraz zasobach naturalnych.
 * <p>
 * Każda instancja {@code Space} przechowuje bezpośrednie referencje do swoich czterech
 * ortogonalnych sąsiadów (góra, dół, lewo, prawo), tworząc strukturę grafu siatki,
 * co pozwala na wydajną nawigację agentów (np. za pomocą algorytmu BFS) bez konieczności
 * ciągłego odpytywania głównej macierzy planszy.
 * </p>
 */
public class Space {

    /** Współrzędna X pola na planszy (kolumna). */
    private int x;

    /** Współrzędna Y pola na planszy (wiersz). */
    private int y;

    /** Flaga określająca, czy pole jest nieprzejezdną barierą architektoniczną (ścianą). */
    private boolean isWall;

    /** Referencja do zasobu środowiskowego ulokowanego na tym polu. */
    private EnvironmentalResource resource;

    /** Lista agentów aktualnie stojących na tym kafelku. */
    private List<Agent> agents = new ArrayList<>();

    /** Lista przedmiotów (uzbrojenie, odzież) leżących na ziemi na tym polu. */
    private List<Equipment> equipmentOnGround = new ArrayList<>();

    /** Referencja do sąsiada z góry (North). */
    private Space up;

    /** Referencja do sąsiada z prawej (East). */
    private Space right;

    /** Referencja do sąsiada z dołu (South). */
    private Space down;

    /** Referencja do sąsiada z lewej (West). */
    private Space left;

    /** Waga kosztu przejścia przez to pole, wykorzystywana przez systemy heurystyczne ruchu AI. */
    private int weigth = 1;

    /** Referencja do strefy bezpieczeństwa, jeżeli pole leży w jej granicach. */
    private SafeZone safeZone = null;

    /**
     * Konstruktor tworzący pole o zadanych współrzędnych geograficznych.
     *
     * @param given_x Współrzędna osi X.
     * @param given_y Współrzędna osi Y.
     */
    public Space(int given_x, int given_y){
        x = given_x;
        y = given_y;
    }

    /**
     * Przekształca to pole w nieprzejezdną ścianę.
     */
    public void createWall(){
        isWall = true;
    }

    /**
     * Sprawdza, czy pole znajduje się w obrębie działającej (niezniszczonej) strefy bezpieczeństwa.
     *
     * @return {@code true} jeśli pole należy do aktywnej strefy; {@code false} w przeciwnym razie.
     */
    public boolean isInSafeZone(){
        return safeZone != null;
    }

    /**
     * Przypisuje pole do konkretnej strefy bezpieczeństwa.
     *
     * @param zone Obiekt zarządzający strefą bezpieczeństwa {@link SafeZone}.
     */
    public void setSafeZone(SafeZone zone) {
        this.safeZone = zone;
    }

    /**
     * Zwraca strefę bezpieczeństwa powiązaną z tym polem.
     *
     * @return Instancja {@link SafeZone} lub {@code null}, jeśli pole nie należy do żadnej strefy.
     */
    public SafeZone getSafeZone() {
        return safeZone;
    }

    /**
     * Sprawdza, czy kafelek jest aktualnie ścianą.
     *
     * @return {@code true} jeśli pole jest ścianą; {@code false} jeśli jest wolną przestrzenią.
     */
    public boolean isItWall(){return isWall;}

    /**
     * Niszczy ścianę (zamienia pole na wolną przestrzeń) i automatycznie raportuje ten fakt
     * do powiązanej strefy bezpieczeństwa w celu zaktualizowania wskaźnika degradacji struktur obronnych.
     */
    public void destroyWall(){
        if (isWall && safeZone != null) {
            safeZone.commitWallDestruction();
        }
        this.isWall = false;
    }

    /**
     * Niszczy ścianę w sposób "cichy" (bez powiadamiania strefy bezpieczeństwa).
     * Wykorzystywane głównie w fazie proceduralnej generacji mapy oraz czyszczenia buforów wokół baz.
     */
    public void destroyWallSilent(){
        this.isWall = false;
    }

    /**
     * Rejestruje referencję górnego sąsiada.
     * @param given_up Pole znajdujące się bezpośrednio wyżej.
     */
    public void joinUp(Space given_up){ up = given_up; }

    /**
     * Rejestruje referencję prawego sąsiada.
     * @param given_right Pole znajdujące się bezpośrednio po prawej.
     */
    public void joinRight(Space given_right){ right = given_right; }

    /**
     * Rejestruje referencję dolnego sąsiada.
     * @param given_down Pole znajdujące się bezpośrednio niżej.
     */
    public void joinDown(Space given_down){ down = given_down; }

    /**
     * Rejestruje referencję lewego sąsiada.
     * @param given_left Pole znajdujące się bezpośrednio po lewej.
     */
    public void joinLeft(Space given_left){ left = given_left; }

    /**
     * Umieszcza agenta na tym polu.
     *
     * @param agent Obiekt agenta wprowadzany na kafelek.
     */
    public void addAgent(Agent agent){ agents.add(agent); }

    /**
     * Usuwa agenta z tego pola (np. w wyniku ruchu lub śmierci).
     *
     * @param agent Obiekt agenta opuszczający kafelek.
     */
    public void deleteAgent(Agent agent){ agents.remove(agent); }

    /**
     * Zwraca listę wszystkich agentów okupujących obecnie to pole.
     *
     * @return Lista referencji do obiektów klasy {@link Agent}.
     */
    public List<Agent> getAgents(){ return agents; }

    /**
     * Pobiera pozycję kafelka w postaci tablicy współrzędnych.
     *
     * @return Dwuelementowa tablica liczb całkowitych w formacie {@code [X, Y]}.
     */
    public int[] getPosition(){ return new int[]{x, y}; }

    /** @return Referencja do sąsiada z góry. */
    public Space getUp(){ return up; }

    /** @return Referencja do sąsiada z prawej. */
    public Space getRight(){ return right; }

    /** @return Referencja do sąsiada z dołu. */
    public Space getDown(){ return down; }

    /** @return Referencja do sąsiada z lewej. */
    public Space getLeft(){ return left; }

    /**
     * Sprawdza, czy na polu znajduje się aktywny, zdatny do zebrania zasób (żywność/medykamenty).
     *
     * @return {@code true} jeśli zasób istnieje i nie został jeszcze wyeksploatowany; {@code false} w przeciwnym razie.
     */
    public boolean containsResource(){
        return resource != null && !resource.wasUsed();
    }

    /**
     * Sprawdza, czy na tym kafelku stoi obecnie przynajmniej jeden agent.
     *
     * @return {@code true} jeśli lista agentów nie jest pusta.
     */
    public boolean containsAgents(){
        return !agents.isEmpty();
    }

    /**
     * Modyfikuje wagę (koszt przejścia) pola o podaną wartość.
     *
     * @param change Wartość (dodatnia lub ujemna) modyfikująca dotychczasowy koszt.
     */
    public void changeWeight(int change){ weigth += change; }

    /**
     * Zwraca aktualną wagę kosztu przejścia dla algorytmów pathfindingu.
     *
     * @return Koszt przejścia jako liczba całkowita.
     */
    public int getWeight(){ return weigth; }

    /**
     * Ciska przedmiot na ziemię (dodaje do lokalnej listy ekwipunku pola).
     *
     * @param equipment Obiekt ekwipunku trafiający na ziemię.
     */
    public void addEquipment(Equipment equipment){ equipmentOnGround.add(equipment); }

    /**
     * Sprawdza, czy na ziemi leżą jakiekolwiek przedmioty gotowe do podniesienia.
     *
     * @return {@code true} jeśli lista ekwipunku na ziemi zawiera elementy.
     */
    public boolean hasEquipment(){ return !equipmentOnGround.isEmpty(); }

    /**
     * Pobiera (usuwa z ziemi) pierwszy przedmiot z brzegu, umożliwiając zebranie go przez agenta.
     *
     * @return Obiekt {@link Equipment} wyciągnięty z ziemi lub {@code null}, jeśli na polu nic nie leżało.
     */
    public Equipment pickUpEquipment(){
        if (!equipmentOnGround.isEmpty()){
            return equipmentOnGround.removeFirst();
        }
        return null;
    }

    /**
     * Zwraca pełną listę przedmiotów znajdujących się aktualnie na ziemi.
     *
     * @return Lista obiektów typu {@link Equipment}.
     */
    public List<Equipment> getEquipmentOnGround(){ return equipmentOnGround; }

    /**
     * Osadza na polu punkt zasobów środowiskowych.
     *
     * @param given_resource Obiekt zasobu {@link EnvironmentalResource}.
     */
    public void addResource(EnvironmentalResource given_resource){ resource = given_resource; }

    /**
     * Pobiera obiekt zasobu przypisany do tego pola.
     *
     * @return Instancja {@link EnvironmentalResource} lub {@code null}.
     */
    public EnvironmentalResource getResource(){ return resource; }
}