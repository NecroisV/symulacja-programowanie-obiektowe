package equipment;

/**
 * Klasa reprezentująca broń (Weapon) jako wyspecjalizowany rodzaj ekwipunku agenta.
 * Implementuje powiązanie z klasą bazową {@link Equipment}.
 * <p>
 * Broń służy do dynamicznego zwiększania potencjału bojowego (siły ataku) ocalałych agentów.
 * Posiada mechanikę zużycia — każde zaangażowanie w walkę obniża jej aktualną wytrzymałość.
 * Efektywna siła broni jest ściśle skorelowana z jej stanem technicznym i spada proporcjonalnie
 * do stopnia jej uszkodzenia (degradacja liniowa).
 * </p>
 */
public class Weapon extends Equipment {

    /** Bazowa (maksymalna) siła rażenia broni, gdy przedmiot jest w pełni sprawny. */
    private int baseStrength;

    /** Aktualna wytrzymałość broni. Spadek do zera oznacza całkowite zużycie lub uszkodzenie przedmiotu. */
    private int actualDurability;

    /** Maksymalna dopuszczalna wytrzymałość strukturalna broni (punkt odniesienia dla kondycji 100%). */
    private int maxDurability;

    /**
     * Konstruktor tworzący egzemplarz broni o określonych parametrach bojowo-użytkowych.
     *
     * @param baseStrength     Maksymalna siła ataku generowana przez nową/naprawioną broń.
     * @param actualDurability Początkowa, bieżąca liczba punktów wytrzymałości przedmiotu.
     * @param maxDurability    Maksymalny pułap punktów wytrzymałości dla tego typu uzbrojenia.
     */
    public Weapon(int baseStrength, int actualDurability, int maxDurability) {
        this.baseStrength = baseStrength;
        this.actualDurability = actualDurability;
        this.maxDurability = maxDurability;
    }

    /**
     * Eksploatuje broń, zmniejszając jej bieżącą wytrzymałość o 1 punkt.
     * Metoda posiada wewnętrzne zabezpieczenie uniemożliwiające spadnięcie wskaźnika
     * wytrzymałości poniżej wartości absolutnego zera (brak wartości ujemnych).
     */
    public void loseDurability() {
        if (actualDurability > 0) {
            actualDurability--;
        }
    }

    /**
     * Oblicza rzeczywistą, aktualną siłę bojową broni na podstawie stopnia jej zużycia.
     * <p>
     * <b>Algorytm skalowania:</b>
     * Wytrzymałość wpływa na siłę w sposób liniowy według wzoru:
     * {@code (baseStrength * actualDurability) / maxDurability}, a wynik jest zaokrąglany
     * do najbliższej liczby całkowitej za pomocą {@link Math#round(double)}.
     * </p>
     * Metoda zawiera bezpiecznik warunkowy — jeżeli {@code maxDurability} wynosi 0,
     * automatycznie zwracane jest 0, co zapobiega krytycznemu wyjątkowi dzielenia przez zero
     * (ArithmeticException).
     *
     * @return Skalowana, całkowita wartość siły bojowej w przedziale [0, baseStrength].
     */
    public int calculateActualStrength() {
        if (maxDurability == 0) {
            return 0;
        } else {
            return (int) Math.round((double) (baseStrength * actualDurability) / maxDurability);
        }
    }

    /**
     * Zwraca bazową (nominalną) siłę broni.
     *
     * @return Stała siła konstrukcyjna broni.
     */
    public int getBaseStrength() {
        return baseStrength;
    }

    /**
     * Modyfikuje bazową siłę broni.
     *
     * @param newStrength Nowa bazowa wartość siły rażenia.
     */
    public void setBaseStrength(int newStrength) {
        this.baseStrength = newStrength;
    }

    /**
     * Zwraca bieżącą wytrzymałość broni.
     *
     * @return Aktualna liczba punktów wytrzymałości.
     */
    public int getActualDurability() {
        return actualDurability;
    }

    /**
     * Bezpiecznie ustawia aktualną wytrzymałość broni.
     * <p>
     * Metoda automatycznie ogranicza (clampuje) przekazaną wartość do poprawnego
     * przedziału logicznego, dbając o to, aby nowa wytrzymałość nie była mniejsza niż 0
     * ani nie przewyższała zdefiniowanego limitu {@code maxDurability}.
     * </p>
     *
     * @param newDurability Żądana wartość wytrzymałości do zaaplikowania.
     */
    public void setActualDurability(int newDurability) {
        this.actualDurability = Math.max(0, Math.min(newDurability, maxDurability));
    }

    /**
     * Zwraca maksymalną możliwą wytrzymałość broni.
     *
     * @return Górna granica punktów sprawności technicznej przedmiotu.
     */
    public int getMaxDurability() {
        return maxDurability;
    }

    /**
     * Definiuje nowy maksymalny próg wytrzymałości strukturalnej broni.
     *
     * @param maxDurability Nowy limit punktów wytrzymałości maksymalnej.
     */
    public void setMaxDurability(int maxDurability) {
        this.maxDurability = maxDurability;
    }

    /**
     * Generuje czytelną, tekstową sygnaturę stanu obiektu broni.
     * Wykorzystywana głównie przez moduły diagnostyczne oraz logi konsoli symulatora.
     *
     * @return Łańcuch znaków zawierający bazową siłę oraz aktualny stosunek wytrzymałości.
     */
    @Override
    public String toString() {
        return "Weapon(strength: " + baseStrength + " durability: " + actualDurability + "/" + maxDurability + ")";
    }
}