package com.mpgames.zone.room;

import java.util.HashMap;
import java.util.Map;

public class Room {
    RoomData data;
    HashMap<String, Player> players;

    public Room() {
    }

    public RoomData getData() {
        return data;
    }

    public void setData(RoomData data) {
        this.data = data;
    }

    public HashMap<String, Player> getPlayers() {
        return players;
    }

    public void setPlayers(HashMap<String, Player> players) {
        this.players = players;
    }


    public void loopThroughPlayers(PlayerLooper playerLooper){
        for(Map.Entry<String, Player> entry : players.entrySet()) {
            String userName = entry.getKey();
            Player player = entry.getValue();
            playerLooper.onPlayerTurn(userName,player);
        }
    }

    public interface PlayerLooper{
        void onPlayerTurn(String userName,Player player);
    }
}
