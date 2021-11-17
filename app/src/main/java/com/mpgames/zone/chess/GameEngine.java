package com.mpgames.zone.chess;

import android.util.Log;
import android.widget.ImageView;

import com.mpgames.zone.Database;
import com.mpgames.zone.Game;
import com.mpgames.zone.R;

import java.util.ArrayList;
import java.util.HashMap;

public class GameEngine {
    EventListener eventListener;
    HashMap<Integer,Piece> data;
    ArrayList<Integer> player1List,player2List;
    private static String TAG = "GameEngine";
    ImageView previousFromImageView,previousToImageView;
    int previousFromKey,previousToKey;
    int king1=0,king2=0;
    boolean leader = false;
    Game game;

    public GameEngine(String roomName) {
        data = new HashMap<>();
        player1List = new ArrayList<>();
        player2List = new ArrayList<>();
        game = new Game(Game.CHESS,roomName);
    }
    public boolean isValidKey(int key){
        String k = ""+key;
        if(k.length()!=2 || key<11 || key>88){
            return false;
        }
        int row = getRowFromKey(key);
        int column = getColumnFromKey(key);
        if(row>0 && row<9 && column>0 && column<9){
            return true;
        }
        return false;
    }

    public ArrayList<Integer> getRawMoves(Piece piece ){
        if(piece==null){
            Log.e(TAG,"getRawMoves piece is null");
        }
        int row = piece.getRow();
        int column = piece.getColumn();
        int player = piece.getPlayer();
        ArrayList<Integer> moves = new ArrayList<>();
        if(piece.isPawn()){
            int add1=0,add2=0;
            if(player==1){
                add1 = -1;
                add2 = -2;
            }else if(player==2){
                add1 = 1;
                add2 = 2;
            }
            int key1 = Integer.parseInt((row+add1)+""+column);
            int key2 = Integer.parseInt((row+add2)+""+column);
            int key3 = Integer.parseInt((row+add1)+""+(column-1));
            int key4 = Integer.parseInt((row+add1)+""+(column+1));
            if(isValidKey(key1) && !data.containsKey(key1)){
                moves.add(key1);
            }
            if(isValidKey(key2) && !data.containsKey(key2) && piece.isFirstMove() && !data.containsKey(key1)){
                moves.add(key2);
            }
            if(isValidKey(key3) && data.containsKey(key3)){
                if(data.get(key3).getPlayer()!=player){
                    moves.add(key3);
                }
            }
            if(isValidKey(key4) && data.containsKey(key4)){
                if(data.get(key4).getPlayer()!=player){
                    moves.add(key4);
                }
            }
        }
        if(piece.isRook()){
            for(int i = row+1; i<9;i++){
                if(data.containsKey(getKey(i,column))){
                    Piece p = data.get(getKey(i,column));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(i,column));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(i,column));
                }
            }
            for(int i = row-1; i>0;i--){
                if(data.containsKey(getKey(i,column))){
                    Piece p = data.get(getKey(i,column));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(i,column));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(i,column));
                }
            }
            for(int i = column+1; i<9;i++){
                if(data.containsKey(getKey(row,i))){
                    Piece p = data.get(getKey(row,i));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(row,i));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(row,i));
                }
            }
            for(int i = column-1; i>0;i--){
                if(data.containsKey(getKey(row,i))){
                    Piece p = data.get(getKey(row,i));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(row,i));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(row,i));
                }
            }
        }
        if(piece.isBishop()){
            for(int r=row-1,c=column+1;r>0 && c<9;r--,c++){
                if(data.containsKey(getKey(r,c))){
                    Piece p = data.get(getKey(r,c));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(r,c));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(r,c));
                }
            }

            for(int r=row-1,c=column-1;r>0 && c>0;r--,c--){
                if(data.containsKey(getKey(r,c))){
                    Piece p = data.get(getKey(r,c));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(r,c));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(r,c));
                }
            }
            for(int r=row+1,c=column+1;r<9 && c<9;r++,c++){
                if(data.containsKey(getKey(r,c))){
                    Piece p = data.get(getKey(r,c));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(r,c));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(r,c));
                }
            }
            for(int r=row+1,c=column-1;r<9 && c>0;r++,c--){
                if(data.containsKey(getKey(r,c))){
                    Piece p = data.get(getKey(r,c));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(r,c));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(r,c));
                }
            }
        }
        if(piece.isKnight()){
            ArrayList<Integer> keyList = new ArrayList<>();
            if(row-1>0 && column-2>0)
                keyList.add(getKey(row-1,column-2));
            if(row-1>0 && column+2<9)
                keyList.add(getKey(row-1,column+2));
            if(row+1<9 && column-2>0)
                keyList.add(getKey(row+1,column-2));
            if(row+1<9 && column+2<9)
                keyList.add(getKey(row+1,column+2));
            if(row-2>0 && column-1>0)
                keyList.add(getKey(row-2,column-1));
            if(row-2>0 && column+1<9)
                keyList.add(getKey(row-2,column+1));
            if(row+2<9 && column-1>0)
                keyList.add(getKey(row+2,column-1));
            if(row+2<9 && column+1<9)
                keyList.add(getKey(row+2,column+1));
            for(int i=0;i<keyList.size();i++){
                int k = keyList.get(i);
                if(data.containsKey(k)){
                    Piece p = data.get(k);
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(k);
                    }
                }else{
                    moves.add(k);
                }
            }
        }
        if(piece.isQueen()) {
            for(int r=row-1,c=column+1;r>0 && c<9;r--,c++){
                if(data.containsKey(getKey(r,c))){
                    Piece p = data.get(getKey(r,c));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(r,c));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(r,c));
                }
            }

            for(int r=row-1,c=column-1;r>0 && c>0;r--,c--){
                if(data.containsKey(getKey(r,c))){
                    Piece p = data.get(getKey(r,c));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(r,c));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(r,c));
                }
            }
            for(int r=row+1,c=column+1;r<9 && c<9;r++,c++){
                if(data.containsKey(getKey(r,c))){
                    Piece p = data.get(getKey(r,c));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(r,c));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(r,c));
                }
            }
            for(int r=row+1,c=column-1;r<9 && c>0;r++,c--){
                if(data.containsKey(getKey(r,c))){
                    Piece p = data.get(getKey(r,c));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(r,c));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(r,c));
                }
            }
            for(int i = row+1; i<9;i++){
                if(data.containsKey(getKey(i,column))){
                    Piece p = data.get(getKey(i,column));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(i,column));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(i,column));
                }
            }
            for(int i = row-1; i>0;i--){
                if(data.containsKey(getKey(i,column))){
                    Piece p = data.get(getKey(i,column));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(i,column));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(i,column));
                }
            }
            for(int i = column+1; i<9;i++){
                if(data.containsKey(getKey(row,i))){
                    Piece p = data.get(getKey(row,i));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(row,i));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(row,i));
                }
            }
            for(int i = column-1; i>0;i--){
                if(data.containsKey(getKey(row,i))){
                    Piece p = data.get(getKey(row,i));
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(getKey(row,i));
                        break;
                    }
                    break;
                }else{
                    moves.add(getKey(row,i));
                }
            }
        }
        if(piece.isKing()){
            ArrayList<Integer> keyList = new ArrayList<>();
            if(row-1>0 && column-1>0)
                keyList.add(getKey(row-1,column-1));
            if(row-1>0)
                keyList.add(getKey(row-1,column));
            if(row-1>0 && column+1<9)
                keyList.add(getKey(row-1,column+1));
            if(column-1>0)
                keyList.add(getKey(row,column-1));
            if(column+1<9)
                keyList.add(getKey(row,column+1));
            if(row+1<9 && column-1>0)
                keyList.add(getKey(row+1,column-1));
            if(row+1<9)
                keyList.add(getKey(row+1,column));
            if(row+1<9 && column+1<9)
                keyList.add(getKey(row+1,column+1));
            for(int i=0;i<keyList.size();i++){
                int k = keyList.get(i);
                if(data.containsKey(k)){
                    Piece p = data.get(k);
                    if(!(p.getPlayer()==piece.getPlayer())){
                        moves.add(k);
                    }
                }else{
                    moves.add(k);
                }
            }
        }
        return moves;
    }

    private boolean timeForPromotion(Move move,Piece piece){
        int row = getRowFromKey(move.getToKey());
        if(move.getPlayerNo()==1 && row==1 && piece.isPawn()){
            return true;
        }else if(move.getPlayerNo()==2 && row==8 && piece.isPawn()){
            return true;
        }
        return false;
    }

    public void executeMove(Move move,ImageView toImageView) {
        int fromKey = move.getFromKey();
        int toKey = move.getToKey();
        int playerNo = move.getPlayerNo();
        if(playerNo==0){
            Log.e(TAG,"executeMove playerUserName=0");
            return;
        }
        Piece piece = data.get(fromKey);
        if(piece!=null){
            ImageView fromImageView = piece.getImageView();
            if(piece.isKing()){
                if(playerNo==1){
                    king1 = toKey;
                }else if(playerNo==2){
                    king2 = toKey;
                }
            }
            int row = getRowFromKey(toKey);
            int column = getColumnFromKey(toKey);
            piece.move(row,column,toImageView);
            if(timeForPromotion(move,piece)){
                piece.setType(move.getPromoteTo());
            }
            data.remove(fromKey);
            removeFromKeyList(playerNo,fromKey);
            if(getPlayerKeyList(getOpponent(playerNo)).contains(toKey)){
                eventListener.onPieceDies(data.get(toKey));
                data.remove(toKey);
                removeFromKeyList(getOpponent(playerNo),toKey);
            }
            addToKeyList(playerNo,toKey);
            data.put(toKey,piece);
            if(playerNo==2){
                hideLastMove();
                fromImageView.setBackgroundColor(fromImageView.getContext().getResources().getColor(R.color.darkOrange));
                toImageView.setBackgroundColor(fromImageView.getContext().getResources().getColor(R.color.orange));
                previousFromImageView = fromImageView;
                previousToImageView = toImageView;
                previousFromKey = fromKey;
                previousToKey = toKey;
            }else if(playerNo==1){
                hideLastMove();
                fromImageView.setBackgroundColor(fromImageView.getContext().getResources().getColor(R.color.darkOrange));
                toImageView.setBackgroundColor(fromImageView.getContext().getResources().getColor(R.color.orange));
                previousFromImageView = fromImageView;
                previousToImageView = toImageView;
                previousFromKey = fromKey;
                previousToKey = toKey;
            }
            checkForResult();
        }else{
            Log.e(TAG,"executeMove Piece is null key "+fromKey);
        }
        Log.e(TAG,"executeMove\n"+data+"\n"+player1List+"\n"+player2List);
    }
    public void hideLastMove(){
        if(previousFromImageView!=null && previousToImageView!=null){
            previousFromImageView.setBackgroundColor(
                    previousFromImageView.getContext().getResources().getColor(
                            getColorID(previousFromKey,false))
            );
            previousToImageView.setBackgroundColor(
                    previousToImageView.getContext().getResources().getColor(
                            getColorID(previousToKey,false))
            );
        }
    }

    public void writeMove(Move move){
        game.writeMove(move);
    }


    public boolean cellIsFilled(int row,int column){
        int key = getKey(row, column);
        if(data.containsKey(key)){
            Piece piece = data.get(key);
            if(piece!=null){
                return true;
            }
        }
        return false;
    }

    public Piece getCellPiece(int row,int column){
        int key = getKey(row, column);
        if(data.containsKey(key)){
            Piece piece = data.get(key);
            if(piece!=null){
                return piece;
            }
        }
        return null;
    }

    private boolean checkEven(int n){
        if(n==2||n==4||n==6||n==8)
            return true;
        return false;
    }
    public int getColorID(int key,boolean selected){
        int row = getRowFromKey(key);
        int column = getColumnFromKey(key);
        if(selected){
            if(checkEven(row)==checkEven(column))
                return R.color.chess_green_box;
            else
                return R.color.chess_darkGreen_box;
        }
        if(checkEven(row)==checkEven(column))
            return R.color.chess_white_box;
        else
            return R.color.chess_black_box;
    }

    public  int getKey(int row, int column){
        return Integer.parseInt(row+""+column);
    }
    public int getRowFromKey(int key){
        return Integer.parseInt((""+key).substring(0,1));
    }
    public int getColumnFromKey(int key){
        return Integer.parseInt((""+key).substring(1));
    }

    public ArrayList<Integer> getPlayerKeyList(int player){
        if(player==1)
            return player1List;
        else if(player==2)
            return player2List;
        return new ArrayList<>();
    }
    private void addToKeyList(int player,int key){
        if(player==1){
            player1List.add(key);
        }
        if(player==2){
            player2List.add(key);
        }
    }
    private void removeFromKeyList(int player,int key){
        if(player==1){
            int index =player1List.indexOf(key);
            if(index>=0 ){
                player1List.remove(index);
            }else{
                Log.e(TAG,"removeFromKeyList key not foung user "+player+" key "+key+" \np1list"+player1List+"\np2list"+player2List);
            }
        }
        if(player==2){
            int index =player2List.indexOf(key);
            if(index>=0){
                player2List.remove(index);
            }else{
                Log.e(TAG,"removeFromKeyList  key not foung user "+player+" key "+key+" \np1list"+player1List+"\np2list"+player2List);
            }
        }
    }

    private int getKingKey(int player){
        if(player==1)
            return king1;
        if(player==2)
            return king2;
        return 0;
    }
    private int getOpponent(int player){
        if(player==1)
            return 2;
        if(player==2)
            return 1;
        return 0;
    }

    public void createPiece(int row, int column, ImageView imageView){
        Piece piece = new Piece(row,column,imageView);
        if(row==1 || row==2){
            piece.setPlayer(2);
        }
        if(row==7 || row==8){
            piece.setPlayer(1);
        }
        if(leader){
            if(piece.getPlayer()==2){
                piece.setWhiteColor();
            }else if(piece.getPlayer()==1){
                piece.setBlackColor();
            }
        }else{
            if(piece.getPlayer()==2){
                piece.setBlackColor();
            }else if(piece.getPlayer()==1){
                piece.setWhiteColor();
            }
        }
        if(row==2 || row==7){
            piece.makePawn();
        }
        if(row==1 || row==8){
            if(column==1 || column==8){
                piece.makeRook();
            }else if(column==2 || column==7){
                piece.makeKnight();
            }else if(column==3 || column==6){
                piece.makeBishop();
            }else if(column==4){
                if(leader){
                    piece.makeKing();
                    if(piece.getPlayer()==1){
                        king1 = getKey(piece.getRow(),piece.getColumn());
                    }else{
                        king2 = getKey(piece.getRow(),piece.getColumn());
                    }
                }else{
                    piece.makeQueen();
                }
            }else if(column==5){
                if(leader){
                    piece.makeQueen();
                }else{
                    piece.makeKing();
                    if(piece.getPlayer()==1){
                        king1 = getKey(piece.getRow(),piece.getColumn());
                    }else{
                        king2 = getKey(piece.getRow(),piece.getColumn());
                    }
                }
            }
        }
        int key = getKey(row, column);
        data.put(key,piece);
        addToKeyList(piece.getPlayer(),key);
    }


    public boolean aCheckOn(int player){
        boolean check = false;
        ArrayList<Integer> keyList = getPlayerKeyList(getOpponent(player));
        ArrayList<Integer> allMoves = new ArrayList<>();
        for(int i=0 ; i<keyList.size() ; i++){
            int key = keyList.get(i);
            Piece piece = data.get(key);
            if(piece!=null){
                allMoves.addAll(getRawMoves(data.get(key)));
            }else{
                Log.e(TAG,"aCheckOn piece is null key "+key);
            }
        }
        if(allMoves.contains(getKingKey(player))) {
            check =  true;
        }
        return check;
    }

    public boolean postMoveCheck(Piece piece , int toKey){
        boolean rightMove;
        int player = piece.getPlayer();
        int toRow = getRowFromKey(toKey);
        int toColumn = getColumnFromKey(toKey);
        int fromRow = piece.getRow();
        int fromColumn = piece.getColumn();
        int fromKey = getKey(fromRow,fromColumn);
        boolean wasKing = false, wasFirstMove = false, wasQueened = false , toKeyExistsInData = false;
        Piece toKeyPieceBackup = null;
        if(piece.isKing()){
            wasKing = true;
            if(player==1){
                king1 = toKey;
            }else{
                king2 = toKey;
            }
        }
        if(piece.isFirstMove()){
            wasFirstMove = true;
            piece.setFirstMove(false);
        }
        piece.setRow(toRow);
        piece.setColumn(toColumn);
        if(toRow==1 && piece.isPawn()){
            wasQueened = true;
            piece.makeQueen();
        }
        data.remove(fromKey);
        removeFromKeyList(player,fromKey);
        if(getPlayerKeyList(getOpponent(player)).contains(toKey)){
            toKeyExistsInData = true;
            toKeyPieceBackup = data.get(toKey);
            removeFromKeyList(getOpponent(player),toKey);
            data.remove(toKey);
        }
        addToKeyList(player,toKey);
        data.put(toKey,piece);
        if(aCheckOn(player)){
            rightMove = false;
        }else{
            rightMove = true;
        }
        if(wasKing){
            if(player==1){
                king1 = fromKey;
            }else{
                king2 = fromKey;
            }
        }
        if(wasFirstMove){
            piece.setFirstMove(true);
        }
        piece.setRow(fromRow);
        piece.setColumn(fromColumn);
        if(wasQueened){
            piece.makePawn();
        }
        data.remove(toKey);
        removeFromKeyList(player,toKey);
        if(toKeyExistsInData){
            addToKeyList(getOpponent(player),toKey);
            data.put(toKey,toKeyPieceBackup);
        }
        addToKeyList(player,fromKey);
        data.put(fromKey,piece);
        return rightMove;
    }
    public ArrayList<Integer> getValidMoves(Piece piece){
        ArrayList<Integer> moves = getRawMoves(piece);
        ArrayList<Integer> newMoves = new ArrayList<>();
        for(int i=0 ; i<moves.size() ; i++){
            int key = moves.get(i);
            if(postMoveCheck(piece,key)){
                newMoves.add(key);
            }
        }
        return newMoves;
    }
    public ArrayList<Integer> getValidMoves(int row,int column){
        return getValidMoves(data.get(getKey(row, column)));
    }

    private int getMoveChoices(int playerNo){
        ArrayList<Integer> keyList = new ArrayList<>();
        keyList.addAll(getPlayerKeyList(playerNo));
        ArrayList<Integer> allmoves = new ArrayList<>();
        for(int i=0;i<keyList.size();){
            int key = keyList.get(i);
            Piece piece = data.get(key);
            if(piece==null){
                Log.e(TAG,"getMoveChoices piece is null");
            }else{
                ArrayList<Integer> moves = getValidMoves(piece);
                allmoves.addAll(moves);
            }
            int index = keyList.indexOf(key);
            keyList.remove(index);
        }
        return allmoves.size();
    }

    private void checkForResult(){
        boolean checkOnP1 = aCheckOn(1);
        boolean checkOnP2 = aCheckOn(2);
        int moveChoicesP1 = getMoveChoices(1);
        int moveChoicesP2 = getMoveChoices(2);
        if(checkOnP1 && moveChoicesP1==0){
            eventListener.onCheckMate(1);
        }else if(checkOnP2 && moveChoicesP2==0){
            eventListener.onCheckMate(2);
        }else if(checkOnP1 && moveChoicesP1!=0){
            eventListener.onCheck(1);
        }else if(checkOnP2 && moveChoicesP2!=0){
            eventListener.onCheck(2);
        }else if((!checkOnP1 && moveChoicesP1==0)||(!checkOnP2 && moveChoicesP2==0)){
            eventListener.onDraw();
        }else if(player1List.size()==1 && player2List.size()==1){
            eventListener.onDraw();
        }
    }

    public interface EventListener {
        public void onCheck(int player);
        public void onCheckMate(int player);
        public void onDraw();
        public void onPieceDies(Piece piece);
    }
    public void setEventListener(EventListener eventListener){
        this.eventListener = eventListener;
    }
}
