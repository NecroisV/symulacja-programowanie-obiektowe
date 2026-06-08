package org.example;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Objects;

public class Render {

    void renderBoard(GraphicsContext gc, double tileSize, List<String> turnLogs, DataCollector data, int actualTick, Space[][] board, Event currentEvent, String timeOfDay) {
        int width = board[0].length;
        int height = board.length;

        double topPanelHeight = 40;

        gc.setFill(Color.web("#1e1e24"));
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        gc.setFill(Color.web("#25252d"));
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), topPanelHeight);

        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));

        gc.setTextAlign(TextAlignment.LEFT);
        if(Objects.equals(timeOfDay, "Dzień")) gc.setFill(Color.web("#ffb703"));
        else gc.setFill(Color.web("#1409e8"));
        gc.fillText("PORA DNIA: " + (timeOfDay != null ? timeOfDay.toUpperCase() : "BRAK DANYCH"), 20, topPanelHeight / 2);

        gc.setTextAlign(TextAlignment.RIGHT);
        if (currentEvent != null) {
            gc.setFill(Color.web("#d90429"));
            String Event = "";
            if (currentEvent instanceof Fog) Event = "FOG";
            else if (currentEvent instanceof Thunderstorm) Event = "THUNDERSTORM";
            else if (currentEvent instanceof Earthquake) Event = "EARTHQUAKE";
            gc.fillText("EVENT: " + Event, gc.getCanvas().getWidth() - 20, topPanelHeight / 2);
        } else {
            gc.setFill(Color.web("#a7a7a7"));
            gc.fillText("EVENT: BRAK AKTUALNYCH ZAGROŻEŃ", gc.getCanvas().getWidth() - 20, topPanelHeight / 2);
        }
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, tileSize * 0.5));

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double x = j * tileSize;
                // Przesunięcie pozycji Y każdego kafelka o wysokość górnego panelu
                double y = topPanelHeight + (i * tileSize);
                Space space = board[i][j];

                if (space.isItWall()) {
                    gc.setFill(Color.web("#ff7e21"));
                    gc.fillRect(x, y, tileSize - 1, tileSize - 1);
                } else if (space.hasEquipment()) {
                    if (space.getEquipmentOnGround().getFirst() instanceof Weapon) {
                        gc.setFill(Color.GRAY);
                        gc.fillRect(x, y, tileSize - 1, tileSize - 1);
                        gc.setFill(Color.BLACK);
                        gc.fillText("W", x + tileSize / 2, y + tileSize / 2);
                    } else if (space.getEquipmentOnGround().getFirst() instanceof Clothes) {
                        gc.setFill(Color.PURPLE);
                        gc.fillRect(x, y, tileSize - 1, tileSize - 1);
                        gc.setFill(Color.BLACK);
                        gc.fillText("C", x + tileSize / 2, y + tileSize / 2);
                    }
                } else if (space.containsResource()) {
                    gc.setFill(Color.BROWN);
                    gc.fillRect(x, y, tileSize - 1, tileSize - 1);
                    gc.setFill(Color.WHITE);
                    gc.fillText("R", x + tileSize / 2, y + tileSize / 2);
                } else if (space.isInSafeZone()) {
                    gc.setFill(Color.web("#1a472a")); // ciemnozielone tło = SafeZone
                    gc.fillRect(x, y, tileSize - 1, tileSize - 1);
                } else {
                    gc.setFill(Color.web("#2d2d35"));
                    gc.fillRect(x, y, tileSize - 1, tileSize - 1);
                }

                // RYSOWANIE AGENTÓW
                if (!space.isItWall()) {
                    List<Agent> agentsOnSpace = space.getAgents();
                    if (!agentsOnSpace.isEmpty()) {
                        double circleRadius = tileSize * 0.8;
                        double offset = (tileSize - circleRadius) / 2;

                        if (agentsOnSpace.size() > 1) {
                            gc.setFill(Color.web("#d90429"));
                            gc.fillOval(x + offset, y + offset, circleRadius, circleRadius);
                            gc.setFill(Color.WHITE);
                            gc.fillText("X", x + tileSize / 2, y + tileSize / 2);
                        } else {
                            Agent a = agentsOnSpace.getFirst();
                            if (a instanceof Survivor) {
                                gc.setFill(Color.web("#00b4d8"));
                                gc.fillOval(x + offset, y + offset, circleRadius, circleRadius);
                                gc.setFill(Color.WHITE);
                                gc.fillText("S", x + tileSize / 2, y + tileSize / 2);
                            } else if (a instanceof Infected) {
                                gc.setFill(Color.web("#38b000"));
                                gc.fillOval(x + offset, y + offset, circleRadius, circleRadius);
                                gc.setFill(Color.WHITE);
                                gc.fillText("I", x + tileSize / 2, y + tileSize / 2);
                            }
                        }
                    }
                }
            }
        }

        // Dolny panel również uwzględnia przesunięcie w dół o topPanelHeight
        double panelStartY = topPanelHeight + (height * tileSize) + 20;

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        gc.setFill(Color.WHITE);
        gc.fillText("TICK: " + actualTick, 15, panelStartY);

        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        gc.fillText("OSTATNIE WYDARZENIA:", 15, panelStartY + 30);

        double logY = panelStartY + 55;
        gc.setFont(Font.font("SansSerif", FontWeight.NORMAL, 13));
        if (turnLogs.isEmpty()) {
            gc.setFill(Color.LIGHTGREEN);
            gc.fillText("Brak wydarzeń w tej turze.", 20, logY);
        } else {
            gc.setFill(Color.web("#ffb703"));
            int displayedLogs = 0;
            for (String log : turnLogs) {
                if (displayedLogs >= 4) break;
                gc.fillText("• " + log, 20, logY);
                logY += 18;
                displayedLogs++;
            }
        }
        turnLogs.clear();

        double statsStartX = Math.max(380, width * tileSize * 0.55);

        gc.setFill(Color.web("#25252d"));
        gc.fillRect(statsStartX - 10, panelStartY, 320, 150);

        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#00b4d8"));
        gc.fillText("LIVE STATS 1", statsStartX, panelStartY + 10);

        gc.setFont(Font.font("SansSerif", FontWeight.NORMAL, 13));
        gc.setFill(Color.WHITE);

        gc.fillText("Ocalali (S): " + data.getSurvivorAmount(), statsStartX, panelStartY + 35);
        gc.fillText("Zakażeni (Z): " + data.getInfectedAmount(), statsStartX, panelStartY + 53);

        gc.fillText("Walka S - Z: " + data.getSurvivorInfectedInteractions(), statsStartX, panelStartY + 71);
        gc.fillText("Walka S - S: " + data.getSurvivorSurvivorInteractions(), statsStartX, panelStartY + 89);

        statsStartX += 350;
        gc.setFill(Color.web("#25252d"));
        gc.fillRect(statsStartX - 10, panelStartY, 320, 150);

        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#00b4d8"));
        gc.fillText("LIVE STATS 2", statsStartX, panelStartY + 10);

        gc.setFont(Font.font("SansSerif", FontWeight.NORMAL, 13));
        gc.setFill(Color.WHITE);

        gc.fillText("Średnie zdrowie: " + String.format("%.1f", data.getMeanHealth()) + " HP", statsStartX, panelStartY + 35);
        gc.fillText("Uleczenia w SafeZone: " + data.getHealedWoundInSafeZones(), statsStartX, panelStartY + 53);

        if (data.getTimeToSurvivorsExtinction() != -1) {
            gc.setFill(Color.web("#d90429"));
            gc.fillText("Zagłada ocalałych w turze: " + data.getTimeToSurvivorsExtinction(), statsStartX, panelStartY + 71);
        } else {
            gc.setFill(Color.LIGHTGREEN);
            gc.fillText("Status ocalałych: Aktywni", statsStartX, panelStartY + 89);
        }
    }
}