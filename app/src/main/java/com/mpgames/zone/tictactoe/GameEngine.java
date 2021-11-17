package com.mpgames.zone.tictactoe;

import android.widget.ImageView;
import com.mpgames.zone.Auth;
import com.mpgames.zone.Game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameEngine {
    private String roomName;
    private boolean myTurn = false;
    private Map<Integer, com.mpgames.zone.tictactoe.Box> data;
    private ArrayList<String> players;
    private EventListener eventListener;
    private boolean gameOver=false;
    public boolean leader = false;
    private Game game;
    public GameEngine(String roomName){
        this.roomName = roomName;
        data = new HashMap<>();
        players = new ArrayList<>();
        game = new Game(Game.TIC_TAC_TOE,roomName);
    }

    public void removeEventListeners(){
        game.removeMoveListener();
        eventListener=null;
    }

    public void executeMove(Move move){
        int playerNo = getPlayerNo(move.getPlayerUserName());
        int key = move.getKey();
        Box box = data.get(key);
        box.setPlayerNo(playerNo);
        box.setFilled(true);
        data.put(key,box);
        checkForResult();
    }

    public void writeMove(Move move){
        game.writeMove(move);
    }
    private void checkForResult(){
        ArrayList<Integer> keyList1 = new ArrayList<>();
        ArrayList<Integer> keyList2 = new ArrayList<>();
        for(int row=1 ; row<4 ; row++){
            for(int column=1 ; column<4 ; column++){
                int key = getKey(row,column);
                Box box = data.get(key);
                if(box.isFilled()){
                    if(box.getPlayerNo()==1)
                        keyList1.add(key);
                    else if(box.getPlayerNo()==2)
                        keyList2.add(key);
                }
            }
        }
        if(playerWon(keyList1)){
            gameOver=true;
            boolean won = Auth.getUserName().equals(players.get(0));
            if(eventListener!=null)
                eventListener.onGameOver(won,players.get(0));
        }else if(playerWon(keyList2)){
            gameOver=true;
            boolean won = Auth.getUserName().equals(players.get(1));
            if(eventListener!=null)
                eventListener.onGameOver(won,players.get(1));
        }else if(keyList1.size()+keyList2.size()==9){
            if(eventListener!=null)
                eventListener.onGameDraw();
        }

    }
    private boolean playerWon(ArrayList<Integer> keyList){
        if(keyList.contains(11) && keyList.contains(12) && keyList.contains(13))
            return true;
        else if(keyList.contains(21) && keyList.contains(22) && keyList.contains(23))
            return true;
        else if(keyList.contains(31) && keyList.contains(32) && keyList.contains(33))
            return true;
        else if(keyList.contains(11) && keyList.contains(21) && keyList.contains(31))
            return true;
        else if(keyList.contains(12) && keyList.contains(22) && keyList.contains(32))
            return true;
        else if(keyList.contains(13) && keyList.contains(23) && keyList.contains(33))
            return true;
        else if(keyList.contains(11) && keyList.contains(22) && keyList.contains(33))
            return true;
        else if(keyList.contains(13) && keyList.contains(22) && keyList.contains(31))
            return true;
        return false;
    }
    public boolean isGameOver(){
        return gameOver;
    }
    public boolean isCellFilled(int key){
        Box box = data.get(key);
        if(box.getPlayerNo()!=0 && box.isFilled())
            return true;
        return false;
    }

    public void createBox(int row, int column, ImageView imageView){
        int key = getKey(row,column);
        Box box = new Box();
        box.setKey(key);
        box.setImageView(imageView);
        box.setPlayerNo(0);
        box.setFilled(false);
        data.put(key,box);
    }

    public int getPlayerNo(String userName){
        return players.indexOf(userName)+1;
    }
    public  int getKey(int row, int column){
        return Integer.parseInt(row+""+column);
    }

    public interface EventListener{
        void onGameOver(boolean won, String winnerUserName);
        void onGameDraw();
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void setPlayers(ArrayList<String> players) {
        this.players = players;
    }
}
