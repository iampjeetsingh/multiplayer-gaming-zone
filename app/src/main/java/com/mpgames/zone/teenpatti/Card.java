package com.mpgames.zone.teenpatti;

public class Card {
    public static int SPADE=4,CLUB=3,HEART=2,DIAMOND=1;
    private int number;
    private int type;

    public Card() {
    }

    public Card(String cardCode) {
        type = Integer.parseInt(cardCode.substring(0,1));
        number = Integer.parseInt(cardCode.substring(1));
    }

    public Card(int number, int type) {
        this.number = number;
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
