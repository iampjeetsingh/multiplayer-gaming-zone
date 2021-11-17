package com.mpgames.zone.room;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mpgames.zone.Auth;
import com.mpgames.zone.Database;
import com.mpgames.zone.Game;

import java.util.HashMap;

public class RoomManager {
    private String TAG = "RoomManager";
    private String roomName;
    private DatabaseReference roomRef, playersRef, dataRef;
    private EventListener eventListener;

    public RoomManager(String roomName) {
        this.roomName = roomName;
        roomRef = Database.getRoomRef(roomName);
        playersRef = roomRef.child("players");
        dataRef = roomRef.child("data");
    }

    public DatabaseReference getReference() {
        return roomRef;
    }

    public void createRoom(){
        Room room = new Room();
        RoomData data = new RoomData();
        data.setGameName("Chess");
        data.setGameStarted(false);
        data.setGameEnded(false);
        room.setData(data);
        HashMap<String,Player> players = new HashMap<>();
        players.put(Auth.getUserName(), Auth.getPlayer());
        room.setPlayers(players);
        roomRef.setValue(room);
        Database.updateCurrentRoom(Auth.getUserName());
    }

    public  void deleteRoom(){
        roomRef.removeValue();
        Database.getGameRef(Game.CHESS,roomName).removeValue();
        Database.getGameRef(Game.CHAIN_REACTION,roomName).removeValue();
        Database.getGameRef(Game.TIC_TAC_TOE,roomName).removeValue();
    }

    public void addPlayer(Player player){
        playersRef.child(player.getUserName()).setValue(player);
        Database.updateCurrentRoom(roomName);
    }

    public void removePlayer(Player player){
        playersRef.child(player.getUserName()).removeValue();
        Database.updateCurrentRoom(null);
    }

    public void setLastMove(Object move){
        playersRef.child(Auth.getUserName()).child("lastMove").setValue(move);
    }
    public void ready(Player player){
        playersRef.child(player.getUserName()).child("ready").setValue(true);
    }
    public void unready(Player player){
        playersRef.child(player.getUserName()).child("ready").setValue(false);
    }
    public void leftGame(Player player){
        playersRef.child(player.getUserName()).child("leftGame").setValue(true);
    }
    public void joinedGame(Player player){
        playersRef.child(player.getUserName()).child("leftGame").setValue(false);
    }
    public void joinedVoiceChat(Player player){
        playersRef.child(player.getUserName()).child("voiceChatOn").setValue(true);
    }
    public void leftVoiceChat(Player player){
        playersRef.child(player.getUserName()).child("voiceChatOn").setValue(false);
    }

    public void updateGame(String gameName){
        dataRef.child("gameName").setValue(gameName);
    }
    public void updateGame(int gameCode){

    }
    public void gameEnded(boolean ended){
        roomRef.child("gameEnded").setValue(ended);
    }
    public void gameStarted(boolean started){
        dataRef.child("gameStarted").setValue(started);
    }

    public String getRoomName(){
        return roomName;
    }

    public interface EventListener{
        void onDataChange(Room room);
        void onRoomDeleted();
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
        roomRef.addValueEventListener(valueEventListener);
    }
    public void removeEventListener(){
        roomRef.removeEventListener(valueEventListener);
        this.eventListener=null;
    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists() && dataSnapshot.hasChild("data")){
                Room room = dataSnapshot.getValue(Room.class);
                if(eventListener!=null)
                    eventListener.onDataChange(room);
            }else{
                if(eventListener!=null)
                    eventListener.onRoomDeleted();
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG,"Listener was cancelled");
        }
    };
}
