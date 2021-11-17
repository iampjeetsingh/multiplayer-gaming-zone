package com.mpgames.zone.chainreaction;

public class Move {
    int key;
    String playerUserName;

    public Move() {
    }

    public Move(int key, String playerUserName) {
        this.key = key;
        this.playerUserName = playerUserName;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getPlayerUserName() {
        return playerUserName;
    }

    public void setPlayerUserName(String playerUserName) {
        this.playerUserName = playerUserName;
    }
}
