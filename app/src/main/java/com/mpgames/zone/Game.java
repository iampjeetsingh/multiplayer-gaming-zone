package com.mpgames.zone;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

public class Game {
    public static int CHESS=1,CHAIN_REACTION=2,TIC_TAC_TOE=3,TEEN_PATTI=4;
    public static int withName(String name){
        if(name==null)
            return 0;
        if(name.equals("Chess"))
            return CHESS;
        else if(name.equals("ChainReaction"))
            return CHAIN_REACTION;
        else if(name.equals("TicTacToe"))
            return TIC_TAC_TOE;
        else if(name.equals("TeenPatti"))
            return TEEN_PATTI;
        return 0;
    }
    public static int getMaxPlayers(int code){
        if(code==CHESS || code==TIC_TAC_TOE)
            return 2;
        else if(code==CHAIN_REACTION)
            return 4;
        else if(code==TEEN_PATTI)
            return 5;
        return 0;
    }

    private String TAG="Game";
    private int code;
    private DatabaseReference movesRef;
    private MoveListener moveListener;

    public Game(int code,String roomName) {
        this.code = code;
        movesRef = Database.getGameRef(code,roomName).child("moves");
    }

    public void writeMove(Object move){
        movesRef.push().setValue(move);
    }

    public void setMoveListener(MoveListener moveListener) {
        this.moveListener = moveListener;
        movesRef.addChildEventListener(childEventListener);
    }

    public void removeMoveListener(){
        movesRef.removeEventListener(childEventListener);
        moveListener = null;
    }

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if(moveListener!=null){
                if(code==CHESS){
                    com.mpgames.zone.chess.Move move = dataSnapshot.getValue(com.mpgames.zone.chess.Move.class);
                    moveListener.onMoveAdded(move);
                }else if(code==CHAIN_REACTION){
                    com.mpgames.zone.chainreaction.Move move = dataSnapshot.getValue(com.mpgames.zone.chainreaction.Move.class);
                    moveListener.onMoveAdded(move);
                }else if(code==TIC_TAC_TOE){
                    com.mpgames.zone.tictactoe.Move move = dataSnapshot.getValue(com.mpgames.zone.tictactoe.Move.class);
                    moveListener.onMoveAdded(move);
                }else if(code==TEEN_PATTI){
                    com.mpgames.zone.teenpatti.Move move = dataSnapshot.getValue(com.mpgames.zone.teenpatti.Move.class);
                    moveListener.onMoveAdded(move);
                }
            }
        }
        @Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
        @Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
        @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG,"Listener was cancelled");
        }
    };

    public interface MoveListener{
        void onMoveAdded(Object moveObj);
    }





}
