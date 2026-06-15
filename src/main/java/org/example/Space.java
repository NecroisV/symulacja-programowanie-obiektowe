package org.example;

import java.util.ArrayList;
import java.util.List;

// Pojedyncze pole na planszy - może zawierać ścianę, agentów, ekwipunek, zasoby
public class Space {
    private int x;
    private int y;
    private boolean isWall;
    private environmentalResource resource;
    private List<Agent> agents = new ArrayList<>();
    private List<Equipment> equipmentOnGround = new ArrayList<>();

    private Space up;
    private Space right;
    private Space down;
    private Space left;

    private int weigth = 1;  // Waga dla systemu poruszania się agentów

    private SafeZone safeZone = null;

    public Space(int given_x, int given_y){
        x = given_x;
        y = given_y;
    }

    public void createWall(){
        isWall = true;
    }

    // Sprawdza czy pole należy do strefy bezpieczeństwa (niezniszczonej)
    public boolean isInSafeZone(){
        return safeZone != null;
    }

    public void setSafeZone(SafeZone zone) {
        this.safeZone = zone;
    }

    public SafeZone getSafeZone() {
        return safeZone;
    }

    public boolean isItWall(){return isWall;}

    // Niszczy ścianę i powiadamia strefę bezpieczeństwa (jeśli należy)
    public void destroyWall(){
        if (isWall && safeZone != null) {
            safeZone.commitWallDestruction();
        }
        this.isWall = false;
    }

    // Niszczy ścianę bez powiadamiania (używane przy generowaniu planszy)
    public void destroyWallSilent(){
        this.isWall = false;
    }

    // Łączenia sąsiednich pól (dla nawigacji)
    public void joinUp(Space given_up){ up = given_up; }
    public void joinRight(Space given_right){ right = given_right; }
    public void joinDown(Space given_down){ down = given_down; }
    public void joinLeft(Space given_left){ left = given_left; }

    public void addAgent(Agent agent){ agents.add(agent); }
    public void deleteAgent(Agent agent){ agents.remove(agent); }
    public List<Agent> getAgents(){ return agents; }

    public int[] getPosition(){ return new int[]{x, y}; }

    public Space getUp(){ return up; }
    public Space getRight(){ return right; }
    public Space getDown(){ return down; }
    public Space getLeft(){ return left; }

    // Sprawdza czy pole zawiera niewykorzystany zasób
    public boolean containsResource(){
        return resource != null && !resource.wasUsed();
    }

    public boolean containsAgents(){
        return !agents.isEmpty();
    }

    public void changeWeight(int change){ weigth += change; }
    public int getWeight(){ return weigth; }

    public void addEquipment(Equipment equipment){ equipmentOnGround.add(equipment); }
    public boolean hasEquipment(){ return !equipmentOnGround.isEmpty(); }

    // Zabiera pierwszy przedmiot z ziemi
    public Equipment pickUpEquipment(){
        if (!equipmentOnGround.isEmpty()){
            return equipmentOnGround.removeFirst();
        }
        return null;
    }

    public List<Equipment> getEquipmentOnGround(){ return equipmentOnGround; }

    public void addResource(environmentalResource given_resource){ resource = given_resource; }
    public environmentalResource getResource(){ return resource; }
}