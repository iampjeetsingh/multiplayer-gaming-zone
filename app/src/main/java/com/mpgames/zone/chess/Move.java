package com.mpgames.zone.chess;

public class Move {
    String playerUserName;
    int fromKey, toKey;
    int playerNo;
    int promoteTo;

    public Move() {
    }

    public Move(String playerUserName, int fromKey, int toKey, int playerNo,  int promoteTo) {
        this.playerUserName = playerUserName;
        this.fromKey = fromKey;
        this.toKey = toKey;
        this.playerNo = playerNo;
        this.promoteTo = promoteTo;
    }

    public String getPlayerUserName() {
        return playerUserName;
    }

    public void setPlayerUserName(String playerUserName) {
        this.playerUserName = playerUserName;
    }

    public int getFromKey() {
        return fromKey;
    }
    public int getToKey() {
        return toKey;
    }

    public void setFromKey(int fromKey) {
        this.fromKey = fromKey;
    }

    public void setToKey(int toKey) {
        this.toKey = toKey;
    }

    public int getPlayerNo() {
        return playerNo;
    }

    public void setPlayerNo(int playerNo) {
        this.playerNo = playerNo;
    }


    public int getPromoteTo() {
        return promoteTo;
    }

    public void setPromoteTo(int promoteTo) {
        this.promoteTo = promoteTo;
    }

    public void setFromKey(int row, int column) {
        this.fromKey = getKey(row, column);
    }

    public void setToKey(int row,int column) {
        this.toKey = getKey(row, column);
    }

    private int getKey(int row, int column){
        return Integer.parseInt(row+""+column);
    }
    private int switchKey(int key){
        int r = 9-getRowFromKey(key);
        int c = 9-getColumnFromKey(key);
        return Integer.parseInt(r+""+c);
    }
    private int getRowFromKey(int key){
        return Integer.parseInt((""+key).substring(0,1));
    }
    private int getColumnFromKey(int key){
        return Integer.parseInt((""+key).substring(1));
    }

    public void switchSides(){
        setFromKey(switchKey(fromKey));
        setToKey(switchKey(toKey));
    }


}
