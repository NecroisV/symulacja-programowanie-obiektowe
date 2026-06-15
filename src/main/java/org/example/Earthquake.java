package org.example;

// Zdarzenie losowe - trzęsienie ziemi niszczy losowe ściany
public class Earthquake implements Event{
    private static double wallDestroyChance = SimulationParameters.getInstance().getEarthquakeWallDestroyChance();

    public Earthquake(){}

    // Wyzwala efekt trzęsienia - każda ściana ma szansę zostać zniszczona
    @Override
    public void trigger(Space[][] board) {
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[i].length; j++){
                if(board[i][j].isItWall()){
                    if(RNG.nextFloat() <= wallDestroyChance) {
                        board[i][j].destroyWall();
                    }
                }
            }
        }
    }
}