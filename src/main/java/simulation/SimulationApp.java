package simulation;

import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Główna klasa aplikacji JavaFX odpowiedzialna za warstwę okienkową i pętlę symulacji.
 * Klasa konfiguruje okno główne (określa wymiary na podstawie rozmiaru siatki), przygotowuje
 * płótno malarskie ({@link Canvas}) oraz uruchamia cykliczny zegar {@link AnimationTimer},
 * który steruje stałym tempem wykonywania kolejnych kroków (ticków) w świecie symulacji.
 */
public class SimulationApp extends Application {

    /** Szerokość planszy wyrażona w liczbie kafelków. */
    private static final int BOARD_WIDTH = 1000;

    /** Wysokość planszy wyrażona w liczbie kafelków. */
    private static final int BOARD_HEIGHT = 600;

    /** Fizyczny rozmiar boku jednego kafelka na ekranie (w pikselach). */
    private static final double TILE_SIZE = 2.0;

    /** Wysokość dolnego panelu przeznaczonego na logi i statystyki (w pikselach). */
    private static final double LOGS_HEIGHT = 160.0;

    /** Wysokość górnego panelu informacyjnego (w pikselach). */
    private static final double TOP_PANEL_HEIGHT = 40;

    /** Referencja do silnika logicznego symulacji. */
    private static SimulationEnvironment environment;

    /**
     * Zwraca aktualną instancję środowiska symulacji.
     * Metoda wykorzystuje wzorzec leniwej inicjalizacji (Lazy Initialization) – tworzy obiekt
     * klasy {@link SimulationEnvironment} przy pierwszym wywołaniu, jeśli ten jeszcze nie istnieje.
     *
     * @return Instancja {@link SimulationEnvironment} zarządzająca stanem i logiką symulacji.
     */
    public static SimulationEnvironment getEnvironment() {
        if(environment == null){
            environment = new SimulationEnvironment(BOARD_WIDTH, BOARD_HEIGHT);
        }
        return environment;
    }

    /**
     * Główny punkt wejścia dla cyklu życia aplikacji JavaFX.
     * Odpowiada za obliczenie finalnych wymiarów okna, utworzenie komponentów graficznych,
     * spięcie ich w strukturę sceny oraz zdefiniowanie i uruchomienie pętli czasu rzeczywistego (game loop).
     * <p>
     * Pętla {@link AnimationTimer} dba o wywoływanie kroku symulacji w stałych odstępach czasu (~4 ticki na sekundę)
     * i zatrzymuje się automatycznie, gdy silnik zasygnalizuje wymarcie ocalałych.
     * </p>
     *
     * @param primaryStage Główna scena (okno) dostarczona przez uruchomieniowe środowisko JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {
        environment = getEnvironment();

        // Dynamiczne wyliczenie całkowitych wymiarów okna na podstawie stałych konfiguracyjnych
        double canvasWidth = BOARD_WIDTH * TILE_SIZE;
        double canvasHeight = (BOARD_HEIGHT * TILE_SIZE) + LOGS_HEIGHT + TOP_PANEL_HEIGHT;

        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, canvasWidth, canvasHeight);

        // Tworzenie i konfiguracja pętli symulacji działającej w tle
        new AnimationTimer() {
            private long lastUpdate = 0;
            private final long TICK_RATE = 256000000;  // Interwał czasowy: ~256ms (ok. 4 ticki na sekundę)

            @Override
            public void handle(long now) {
                // Sprawdzenie czy upłynęło wystarczająco dużo czasu od ostatniego kroku
                if (now - lastUpdate >= TICK_RATE) {
                    boolean running = environment.simulationStep(gc, TILE_SIZE);

                    // Jeśli metoda krokowa zwróci false (brak ocalałych), pętla zostaje przerwana
                    if (!running) {
                        this.stop();
                        System.out.println("Symulacja zakończona: wszyscy ocalali zginęli.");
                    }

                    lastUpdate = now;
                }
            }
        }.start();

        primaryStage.setTitle("Symulacja");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Metoda rozruchowa JavaFX. Służy do uruchomienia podsystemu graficznego,
     * gdy aplikacja nie jest wywoływana bezpośrednio przez kontener narzędziowy.
     *
     * @param args Parametry wiersza poleceń przekazywane przy starcie.
     */
    public static void main(String[] args) {
        launch(args);
    }
}