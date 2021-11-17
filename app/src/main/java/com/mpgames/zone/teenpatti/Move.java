package com.mpgames.zone.teenpatti;

public class Move {
    public static int CHAAL=0,RAISE=1,PACK=2,SHOW=3,BLIND=4;

    private int action,amount;
    private String playerUserName;

    public Move() {
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getPlayerUserName() {
        return playerUserName;
    }

    public void setPlayerUserName(String playerUserName) {
        this.playerUserName = playerUserName;
    }
}
