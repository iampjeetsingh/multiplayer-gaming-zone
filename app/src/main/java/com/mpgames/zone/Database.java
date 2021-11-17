package com.mpgames.zone;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class Database {
    public static DatabaseReference getRoot(){
        return FirebaseDatabase.getInstance().getReference();
    }
    public static DatabaseReference getUsersRef(){
        return getRoot().child("Users");
    }
    public static DatabaseReference getUserRef(){
        return getUsersRef().child(Auth.getUserName());
    }
    public static DatabaseReference getUserRef(String userName) {
        return getUsersRef().child(userName);
    }
    public static DatabaseReference getInviteRef(){
        return getRoot().child("Invites").child(Auth.getUserName());
    }
    public static DatabaseReference getInviteRef(String username){
        return getRoot().child("Invites").child(username);
    }
    public static DatabaseReference getRoomRef(String roomName){
        return getRoot().child("Rooms").child(roomName);
    }
    public static DatabaseReference getGameRef(String gameName,String roomName){
        return getRoot().child(gameName).child(roomName);
    }
    public static DatabaseReference getGameRef(int code,String roomName){
        String gameName = null;
        if(code==Game.CHESS)
            gameName="Chess";
        else if(code==Game.CHAIN_REACTION)
            gameName="ChainReaction";
        else if(code==Game.TIC_TAC_TOE)
            gameName="TicTacToe";
        else if(code==Game.TEEN_PATTI)
            gameName="TeenPatti";
        if(gameName==null)
            return null;
        return getRoot().child(gameName).child(roomName);
    }
    public static DatabaseReference getConnectionRef(){
        return FirebaseDatabase.getInstance().getReference(".info/connected");
    }
    public static void updateCurrentRoom(String roomName){
        Map<String ,Object> map = new HashMap<>();
        if(roomName==null){
            map.put("inRoom",false);
            map.put("currentRoomName",null);
        }else{
            map.put("inRoom",true);
            map.put("currentRoomName",roomName);
        }
        getUserRef().updateChildren(map);
    }
    public static Connection getConnection(){
        return new Connection();
    }
}
