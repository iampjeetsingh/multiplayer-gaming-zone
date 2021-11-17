package com.mpgames.zone.chainreaction;

import android.widget.ImageView;
import com.mpgames.zone.Auth;
import com.mpgames.zone.Game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameEngine {
    private String TAG = "GameEngine";
    private Map<Integer,Box> data;
    private ArrayList<String> players;
    private ArrayList<Integer> outPlayers;
    private int turn = 1;
    private int moveNo=1;
    private EventListener eventListener;
    private boolean gameOver =  false;
    private Game game;
    private ArrayList<Move> movesQue;

    public GameEngine(String roomName,ArrayList<String> players) {
        data = new HashMap();
        this.players = players;
        outPlayers = new ArrayList<>();
        movesQue = new ArrayList<>();
        game = new Game(Game.CHAIN_REACTION,roomName);
        game.setMoveListener(new Game.MoveListener() {
            @Override
            public void onMoveAdded(Object moveObj) {
                executeMove((Move)moveObj);
            }
        });
    }
    public void removeListeners(){
        game.removeMoveListener();
        eventListener = null;
    }

    public void writeMove(Move move){
        game.writeMove(move);
    }

    private void executeMove(Move move){
        if(players.size()==0){
            movesQue.add(move);
        }
        int playerNo = getPlayerNo(move.getPlayerUserName());
        int key = move.getKey();
        executeAction(key,getAction(key),playerNo);
        switchTurn();
        moveNo++;
        checkForResult();
    }

    private void checkForResult(){
        if(moveNo<=players.size()){
            return;
        }
        ArrayList<Integer> playerNos = new ArrayList<>();
        for(int row=1 ; row<9 ; row++){
            for(int column=1 ; column<7 ; column++){
                int key = getKey(row,column);
                Box box = getBox(key);
                if(box.getPlayerNo()!=0 && !playerNos.contains(box.getPlayerNo())){
                    playerNos.add(box.getPlayerNo());
                }
            }
        }
        for(int p=1 ; p<=players.size() ; p++){
            if(!playerNos.contains(p)){
                outPlayers.add(p);
            }
        }
        if(playerNos.size()==1){
            int playerNo = playerNos.get(0);
            boolean won = playerNo==getPlayerNo(Auth.getUserName());
            String winner = players.get(playerNo-1);
            gameOver=true;
            eventListener.onGameResult(won,winner);
        }
    }

    private void switchTurn(){
        turn++;
        int totalPlayers = players.size();
        if(turn >totalPlayers){
            turn = turn -totalPlayers;
        }
        if(outPlayers.contains(turn)) {
            switchTurn();
        }else{
            boolean userTurn = (turn==getPlayerNo(Auth.getUserName()));
            eventListener.onTurnChange(userTurn,players.get(turn-1));
        }
    }

    private void executeAction(int key,int action,int playerNo){
        if(action==1){
            upgrade(key,playerNo);
        }else if(action==2){
            blast(key,playerNo);
        }
    }

    private void upgrade(int key,int playerNo){
        Box box = data.get(key);
        box.setPlayerNo(playerNo);
        box.setColor(getPlayerColor(playerNo));
        box.upgrade();
        data.put(key,box);
    }

    private void blast(int key,int playerNo){
        Box box = data.get(key);
        box.blast();
        data.put(key,box);
        int r = getRowFromKey(key);
        int c = getColumnFromKey(key);
        int key1 = getKey(r-1,c);
        int key2 = getKey(r+1,c);
        int key3 = getKey(r,c-1);
        int key4 = getKey(r,c+1);
        if(isValidKey(key1)){
            executeAction(key1,getAction(key1),playerNo);
        }
        if(isValidKey(key2)){
            executeAction(key2,getAction(key2),playerNo);
        }
        if(isValidKey(key3)){
            executeAction(key3,getAction(key3),playerNo);
        }
        if(isValidKey(key4)){
            executeAction(key4,getAction(key4),playerNo);
        }
    }

    public int getPlayerNo(String userName){
        return players.indexOf(userName)+1;
    }

    private int getAction(int key){
        int row = getRowFromKey(key);
        int column = getColumnFromKey(key);
        Box box = data.get(key);
        int value = box.getValue();
        if(value==0){
            return 1;
        }
        if(row==1 || row==8 || column==1 || column==6){
            if((row==1 || row==8) && (column==1 || column==6)){
                if(value==1)
                    return 2;
            }else{
                if(value==1)
                    return 1;
                if(value==2)
                    return 2;
            }
        }else{
            if(value==1 || value==2)
                return 1;
            if(value==3)
                return 2;
        }
        return 0;
    }

    private int getPlayerColor(int playerNo){
        return playerNo;
    }
    public Box getBox(int key){
        return data.get(key);
    }

    public boolean isMyTurn(){
        return turn == getPlayerNo(Auth.getUserName());
    }
    public boolean isGameOver(){
        return gameOver;
    }

    public boolean isCellFilled(int key){
        Box box = data.get(key);
        return box.getPlayerNo()!=0 && box.getValue()!=0;
    }

    public void createBox(int row, int column, ImageView imageView){
        int key = getKey(row,column);
        Box box = new Box();
        box.setRow(row);
        box.setColumn(column);
        box.setImageView(imageView);
        box.setPlayerNo(0);
        box.setValue(0);
        data.put(key,box);
    }

    private boolean isValidKey(int key){
        String k = ""+key;
        if(k.length()!=2 || key<11 || key>86){
            return false;
        }
        int row = getRowFromKey(key);
        int column = getColumnFromKey(key);
        return row>0 && row<9 && column>0 && column<7;
    }

    public  int getKey(int row, int column){
        return Integer.parseInt(row+""+column);
    }
    private int getRowFromKey(int key){
        return Integer.parseInt((""+key).substring(0,1));
    }
    private int getColumnFromKey(int key){
        return Integer.parseInt((""+key).substring(1));
    }

    public interface EventListener{
        void onGameResult(boolean won,String winnerUserName);
        void onTurnChange(boolean userTurn,String userName);
    }
    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

}
