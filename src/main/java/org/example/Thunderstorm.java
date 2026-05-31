package org.example;

public class Thunderstorm implements Event{

    public Thunderstorm(){}

    @Override
    public void trigger(Space[][] board) {
        int X = RNG.nextInt(board.length);
        int Y = RNG.nextInt(board[0].length);

        if(board[X][Y].isItWall()){
            board[X][Y].destroyWall();
        }

        for(Agent agent : board[X][Y].getAgents()){
            agent.die();
        }

// Teoretycznie też działa
//        board[X][Y].getAgents().clear();
    }
}
