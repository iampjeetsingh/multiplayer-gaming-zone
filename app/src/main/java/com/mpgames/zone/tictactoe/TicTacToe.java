package com.mpgames.zone.tictactoe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.OnDisconnect;
import com.mpgames.zone.App;
import com.mpgames.zone.Auth;
import com.mpgames.zone.Connection;
import com.mpgames.zone.Database;
import com.mpgames.zone.Game;
import com.mpgames.zone.PermissionsActivity;
import com.mpgames.zone.PlayerTile;
import com.mpgames.zone.room.Player;
import com.mpgames.zone.room.Room;
import com.mpgames.zone.room.RoomManager;
import com.mpgames.zone.R;
import com.mpgames.zone.dialog.ConfirmDialog;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicTacToe extends AppCompatActivity {
    private static String TAG = "TicTacToe";
    private Context context = TicTacToe.this;

    private App app;

    private ImageView box11,box12,box13,box21,box22,box23,box31,box32,box33;
    private PlayerTile player1Tile,player2Tile;
    private TextView turntxt;
    private ImageView voiceChatIndicator;

    private View view;
    private LinearLayout mainContainer;

    private boolean intialTurnUpdated = false;
    private boolean allOnline,allPlaying;
    private boolean myTurn=false;
    private boolean leader=false;
    private GameEngine gameEngine;
    private RoomManager roomManager;
    private Connection connection;
    private Game game;
    private DatabaseReference roomRef,playerRef;

    private OnDisconnect online,playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_toe);
        initialiseViews();
        setUpClickListeners();

        app = (App) getApplication();
        connection = Database.getConnection();

        String roomName = getIntent().getStringExtra("roomName");
        roomManager = new RoomManager(roomName);
        game = new Game(Game.TIC_TAC_TOE,roomName);

        if(Auth.getUserName().equals(roomName)){
            leader = true;
            turntxt.setText("Opponent's Turn");
        }else
            myTurn = true;
        gameEngine = new GameEngine(roomName);
        if(leader)
            gameEngine.leader = true;
        initialiseGameEngine();
        roomRef = Database.getRoomRef(roomName);
        playerRef = roomRef.child("players").child(Auth.getUserName());
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(app.context==context){
            app.setContext(null);
        }
        removeEventListeners();
        playerRef.child("playing").setValue(false);
        if(app.call!=null){
            app.call.removeCallListener(callListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
        setUpEventListeners();
        if(app.call!=null){
            voiceChatIndicator.setImageResource(R.drawable.ic_mic);
            app.call.addCallListener(callListener);
        }else
            voiceChatIndicator.setImageResource(R.drawable.ic_mic_off);
    }

    //On User Input
    @Override
    public void onBackPressed() {
        ConfirmDialog confirmDialog = new ConfirmDialog(context,
                "Exit Game",
                "Do you want to exit the game?",
                new Runnable() {
                    @Override
                    public void run() {
                        online.cancel();
                        playing.cancel();
                        roomManager.gameStarted(false);
                        roomManager.leftGame(Auth.getPlayer());
                        finish();
                    }
                },null);
        confirmDialog.show();
    }
    private void userClick(int row,int column){
        int key = gameEngine.getKey(row,column);
        if(gameEngine.isCellFilled(key) || !myTurn || gameEngine.isGameOver()){
            return;
        }
        Move move = new Move(key, Auth.getUserName());
        gameEngine.writeMove(move);
    }
    public void voiceChatClick(View v){
        if(app.call!=null){
            app.call.hangup();
            return;
        }
        if(app.checkPermissions()){
            app.call = app.callClient.callConference(roomManager.getRoomName());
            app.call.addCallListener(callListener);
        }else{
            Intent intent = new Intent(context, PermissionsActivity.class);
            startActivityForResult(intent,7);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==7 && resultCode==RESULT_OK){
            app.call = app.callClient.callConference(roomManager.getRoomName());
            app.call.addCallListener(callListener);
        }
    }

    //Setting Up Event Listeners
    private void setUpEventListeners(){
        roomManager.setEventListener(roomEventListener);
        game.setMoveListener(moveListener);
        gameEngine.setEventListener(gameEventListener);
        connection.setEventListener(connectionListener);
    }
    private void removeEventListeners(){
        roomManager.removeEventListener();
        gameEngine.removeEventListeners();
        connection.removeEventListener();
    }
    RoomManager.EventListener roomEventListener = new RoomManager.EventListener() {
        @Override
        public void onDataChange(Room room) {
            final ArrayList<String> players = new ArrayList<>();
            allOnline=true;
            allPlaying=true;
            final Object[] opponentLeft = {false,""};
            room.loopThroughPlayers((userName, player) -> {
                players.add(player.getUserName());
                if(!player.isOnline())
                    allOnline=false;
                if(!player.isPlaying())
                    allPlaying=false;
                if(Auth.isCurrentPlayer(player)) {
                    updatePlayerInfo(1, player);
                }else{
                    updatePlayerInfo(2,player);
                    if(player.isLeftGame()){
                        opponentLeft[0] =true;
                        opponentLeft[1] = player.getUserName();
                    }
                }
            });
            Collections.sort(players);
            if(players.indexOf(Auth.getUserName())==1 && !intialTurnUpdated){
                turntxt.setText("Opponent's Turn");
                intialTurnUpdated=true;
            }
            gameEngine.setPlayers(players);
            if((boolean)opponentLeft[0]){
                Toast.makeText(context,"Opponent Left the Game",Toast.LENGTH_SHORT).show();
                online.cancel();
                playing.cancel();
                finish();
            }
        }
        @Override
        public void onRoomDeleted() {
            Toast.makeText(context,"Leader left the Room",Toast.LENGTH_SHORT).show();
            online.cancel();
            playing.cancel();
            finish();
        }
    };
    Game.MoveListener moveListener = new Game.MoveListener() {
        @Override
        public void onMoveAdded(Object moveObj) {
            Move move = (Move) moveObj;
            gameEngine.executeMove(move);
            switchTurn();
        }
    };
    GameEngine.EventListener gameEventListener = new GameEngine.EventListener() {
        @Override
        public void onGameOver(boolean won, String winnerUserName) {
            if(won){
                Snackbar.make(view,"You won the game",Snackbar.LENGTH_INDEFINITE).show();
            }else{
                Snackbar.make(view,winnerUserName+" won  the game",Snackbar.LENGTH_INDEFINITE).show();
            }
        }
        @Override
        public void onGameDraw() {
            Snackbar.make(view,"Its a DRAW",Snackbar.LENGTH_INDEFINITE).show();
        }
    };
    Connection.EventListener connectionListener = new Connection.EventListener(){
        @Override
        public void onConnected() {
            playerRef.child("online").setValue(true);
            playerRef.child("playing").setValue(true);
            online= playerRef.child("online").onDisconnect();
            playing =  playerRef.child("playing").onDisconnect();
            online.setValue(false);
            playing.setValue(false);
        }
        @Override
        public void onDisconnected() {
        }
    };
    CallListener callListener = new CallListener() {
        @Override
        public void onCallProgressing(Call call) {
            voiceChatIndicator.setImageResource(R.drawable.ic_mic_none);
        }
        @Override
        public void onCallEstablished(Call call) {
            roomManager.joinedVoiceChat(Auth.getPlayer());
            Toast.makeText(context,"Voice Chat Connected",Toast.LENGTH_SHORT).show();
            voiceChatIndicator.setImageResource(R.drawable.ic_mic);
        }
        @Override
        public void onCallEnded(Call call) {
            app.call.removeCallListener(callListener);
            app.call = null;
            roomManager.leftVoiceChat(Auth.getPlayer());
            Toast.makeText(context,"Voice Chat Ended",Toast.LENGTH_SHORT).show();
            voiceChatIndicator.setImageResource(R.drawable.ic_mic_off);
        }
        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {
        }
    };

    private void switchTurn(){
        if(myTurn){
            turntxt.setText("Opponent's Turn");
            myTurn = false;
        } else {
            turntxt.setText("Your Turn");
            myTurn = true;
        }
    }


    //Update Players Information
    private void updatePlayerInfo(int tileNo, Player player){
        PlayerTile tile = getPlayerTile(tileNo);
        if(tile!=null){
            tile.setName(player.getName());
            tile.setDescription(player.getUserName());
            tile.setStatusIndicator(player.isOnline(),player.isPlaying());
            tile.setProfilePhoto(player.getPhotoUrl());
            if(allOnline){
                if(allPlaying)
                    mainContainer.setBackgroundColor(getResources().getColor(R.color.green));
                else
                    mainContainer.setBackgroundColor(getResources().getColor(R.color.yellow));
            }else
                mainContainer.setBackgroundColor(getResources().getColor(R.color.red));
        }
    }
    private PlayerTile getPlayerTile(int tileNo){
        PlayerTile tile = null;
        if(tileNo==1)
            tile = player1Tile;
        else if(tileNo==2)
            tile = player2Tile;
        return tile;
    }

    //Initialisation
    private void initialiseViews(){
        mainContainer = findViewById(R.id.mainContainer);
        view = mainContainer;
        turntxt = findViewById(R.id.turntxt);
        voiceChatIndicator = findViewById(R.id.voicechatimg);
        player1Tile = new PlayerTile(
                findViewById(R.id.p1nametxt),
                findViewById(R.id.p1desctxt),
                findViewById(R.id.p1statusimg),
                findViewById(R.id.p1playerimg));
        player2Tile = new PlayerTile(
                findViewById(R.id.p2nametxt),
                findViewById(R.id.p2desctxt),
                findViewById(R.id.p2statusimg),
                findViewById(R.id.p2playerimg));
        box11 = findViewById(R.id.box11);
        box12 = findViewById(R.id.box12);
        box13 = findViewById(R.id.box13);
        box21 = findViewById(R.id.box21);
        box22 = findViewById(R.id.box22);
        box23 = findViewById(R.id.box23);
        box31 = findViewById(R.id.box31);
        box32 = findViewById(R.id.box32);
        box33 = findViewById(R.id.box33);
    }
    private void setUpClickListeners(){
        box11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(1,1);
            }
        });
        box12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(1,2);
            }
        });
        box13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(1,3);
            }
        });
        box21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(2,1);
            }
        });
        box22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(2,2);
            }
        });
        box23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(2,3);
            }
        });
        box31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(3,1);
            }
        });
        box32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(3,2);
            }
        });
        box33.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userClick(3,3);
            }
        });
    }
    private void initialiseGameEngine(){
        for(int row=1 ; row<4 ; row++){
            for(int column=1 ; column<4 ; column++){
                gameEngine.createBox(row,column,getBoxImageView(row,column));
            }
        }
    }


    //Get ImageView of Specific Box
    private ImageView getBoxImageView(int row, int column){
        if(row==1 && column==1) return box11;
        if(row==1 && column==2) return box12;
        if(row==1 && column==3) return box13;
        if(row==2 && column==1) return box21;
        if(row==2 && column==2) return box22;
        if(row==2 && column==3) return box23;
        if(row==3 && column==1) return box31;
        if(row==3 && column==2) return box32;
        if(row==3 && column==3) return box33;
        return null;
    }
}
