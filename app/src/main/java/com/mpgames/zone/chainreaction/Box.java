package com.mpgames.zone.chainreaction;

import android.widget.ImageView;
import com.mpgames.zone.R;

public class Box {
    int row,column,value,playerNo,color;
    ImageView imageView;

    public Box() {
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getPlayerNo() {
        return playerNo;
    }

    public void setPlayerNo(int playerNo) {
        this.playerNo = playerNo;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void upgrade(){
        value++;
        imageView.setImageResource(getImageResource());
    }
    public void blast(){
        value = 0;
        playerNo = 0;
        imageView.setImageDrawable(null);
    }
    private int getImageResource(){
        if(color==1){
            if(value==1){
                return R.drawable.blue1;
            }else if(value==2){
                return R.drawable.blue2;
            }else if(value==3){
                return R.drawable.blue3;
            }
        }else if(color==2){
            if(value==1){
                return R.drawable.green1;
            }else if(value==2){
                return R.drawable.green2;
            }else if(value==3){
                return R.drawable.green3;
            }
        }else if(color==3){
            if(value==1){
                return R.drawable.yellow1;
            }else if(value==2){
                return R.drawable.yellow2;
            }else if(value==3){
                return R.drawable.yellow3;
            }
        }else if(color==4){
            if(value==1){
                return R.drawable.red1;
            }else if(value==2){
                return R.drawable.red2;
            }else if(value==3){
                return R.drawable.red3;
            }
        }
        return 0;
    }
}
