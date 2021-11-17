package com.mpgames.zone.tictactoe;

import android.widget.ImageView;

import com.mpgames.zone.R;

public class Box {
    int key,playerNo;
    ImageView imageView;
    boolean filled;

    public Box() {
    }

    public void setKey(int key) {
        this.key = key;
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

    public boolean isFilled() {
        return filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
        if(filled){
            if(playerNo==1){
                imageView.setImageResource(R.drawable.x);
            }else if(playerNo==2){
                imageView.setImageResource(R.drawable.o);
            }
        }else{
            imageView.setImageDrawable(null);
        }

    }


}
