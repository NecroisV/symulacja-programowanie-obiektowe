package org.example;

public class Earthquake implements Event{
    private static double wallDestroyChance = SimulationParameters.getInstance().getEarthquakeWallDestroyChance();
    public Earthquake(){}

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
