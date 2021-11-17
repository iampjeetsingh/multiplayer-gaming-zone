package com.mpgames.zone.chess;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.mpgames.zone.R;

public class Piece {
    private int row,column,type=0,player=0;
    private boolean firstMove=true;
    private ImageView imageView;
    private int color;

    public Piece(int row, int column, ImageView imageView) {
        this.row = row;
        this.column = column;
        this.imageView = imageView;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
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

    public boolean isPawn(){
        if(type==0){
            return true;
        }
        return false;
    }
    public void makePawn(){
        type=0;
        imageView.setImageResource(getResource(color));
    }

    public boolean isRook(){
        if(type==1){
            return true;
        }
        return false;
    }
    public void makeRook(){
        type=1;
        imageView.setImageResource(getResource(color));
    }

    public boolean isKnight(){
        if(type==2){
            return true;
        }
        return false;
    }
    public void makeKnight(){
        type=2;
        imageView.setImageResource(getResource(color));
    }

    public boolean isBishop(){
        if(type==3){
            return true;
        }
        return false;
    }
    public void makeBishop(){
        type=3;
        imageView.setImageResource(getResource(color));
    }

    public boolean isQueen(){
        if(type==4){
            return true;
        }
        return false;
    }
    public void makeQueen(){
        type=4;
        imageView.setImageResource(getResource(color));
    }

    public boolean isKing(){
        if(type==5){
            return true;
        }
        return false;
    }
    public void makeKing(){
        type=5;
        imageView.setImageResource(getResource(color));
    }

    public void move(int row, int column, ImageView imageView){
        if(firstMove){
            firstMove=false;
        }
        Drawable drawable = this.imageView.getDrawable();
        this.imageView.setImageDrawable(null);
        this.imageView = imageView;
        imageView.setImageDrawable(drawable);
        setRow(row);
        setColumn(column);
    }

    public void setWhiteColor(){
        setColor(2);
    }

    public void setBlackColor(){
        setColor(1);
    }

    private int getResource(int color){
        if(color==1){
            if(isPawn()){
                return R.drawable.pawn;
            }else if(isRook()){
                return R.drawable.rook;
            }else if(isKnight()){
                return R.drawable.knight;
            }else if(isBishop()){
                return R.drawable.bishop;
            }else if(isQueen()){
                return R.drawable.queen;
            }else if(isKing()){
                return R.drawable.king;
            }
        }else if(color==2){
            if(isPawn()){
                return R.drawable.pawn2;
            }else if(isRook()){
                return R.drawable.rook2;
            }else if(isKnight()){
                return R.drawable.knight2;
            }else if(isBishop()){
                return R.drawable.bishop2;
            }else if(isQueen()){
                return R.drawable.queen2;
            }else if(isKing()){
                return R.drawable.king2;
            }
        }
        return 0;
    }

    public void setType(int type){
        if(type==1){
            makeRook();
        }else if(type==2){
            makeKnight();
        }else if(type==3){
            makeBishop();
        }else if(type==4){
            makeQueen();
        }
    }
    public int getType(){
        return type;
    }
}
