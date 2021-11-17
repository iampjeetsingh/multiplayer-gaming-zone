package com.mpgames.zone;

import android.content.Context;
import android.content.Intent;

import com.mpgames.zone.chainreaction.ChainReaction;
import com.mpgames.zone.chess.ChessActivity;
import com.mpgames.zone.teenpatti.TeenPatti;
import com.mpgames.zone.tictactoe.TicTacToe;

import java.util.ArrayList;

public class Actions {
    private static void startChess(Context context, String roomName){
        Intent intent = new Intent(context, ChessActivity.class);
        intent.putExtra("roomName",roomName);
        context.startActivity(intent);
    }
    private static void startChainReaction(Context context, String roomName, ArrayList<String> players){
        Intent intent = new Intent(context, ChainReaction.class);
        intent.putExtra("roomName",roomName);
        String[] array = new String[players.size()];
        for(int i=0 ; i<players.size() ; i++){
            array[i] = players.get(i);
        }
        intent.putExtra("players",array);
        context.startActivity(intent);
    }
    private static void startTicTacToe(Context context, String roomName){
        Intent intent = new Intent(context, TicTacToe.class);
        intent.putExtra("roomName",roomName);
        context.startActivity(intent);
    }
    private static void startTeenPatti(Context context, String roomName, ArrayList<String> players){
        Intent intent = new Intent(context, TeenPatti.class);
        intent.putExtra("roomName",roomName);
        String[] array = new String[players.size()];
        for(int i=0 ; i<players.size() ; i++){
            array[i] = players.get(i);
        }
        intent.putExtra("players",array);
        context.startActivity(intent);
    }
    public static void startGame(int gameCode, Context context, String roomName, ArrayList<String> players){
        if(gameCode==Game.CHESS){
            startChess(context, roomName);
        }else if(gameCode==Game.CHAIN_REACTION){
            startChainReaction(context, roomName,players);
        }else if(gameCode==Game.TIC_TAC_TOE){
            startTicTacToe(context, roomName);
        }else if(gameCode==Game.TEEN_PATTI){
            startTeenPatti(context, roomName,players);
        }
    }
}
