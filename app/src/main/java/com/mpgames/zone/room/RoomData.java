package com.mpgames.zone.room;

public class RoomData{

    boolean gameStarted,gameEnded;
    String gameName;

    public RoomData() {
    }

    public RoomData(boolean gameStarted, boolean gameEnded, String gameName) {
        this.gameStarted = gameStarted;
        this.gameEnded = gameEnded;
        this.gameName = gameName;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public void setGameEnded(boolean gameEnded) {
        this.gameEnded = gameEnded;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
}
