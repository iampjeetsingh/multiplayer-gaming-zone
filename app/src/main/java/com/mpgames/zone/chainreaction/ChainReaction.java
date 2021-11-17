package com.mpgames.zone.chainreaction;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChainReaction extends AppCompatActivity {
    private static String TAG = "ChainReaction";
    private Context context = ChainReaction.this;

    private App app;

    private ImageView
            box11,box12,box13,box14,box15,box16, box21,box22,box23,box24,box25,box26,
            box31,box32,box33,box34,box35,box36, box41,box42,box43,box44,box45,box46,
            box51,box52,box53,box54,box55,box56, box61,box62,box63,box64,box65,box66,
            box71,box72,box73,box74,box75,box76, box81,box82,box83,box84,box85,box86;
    private PlayerTile player1Tile,player2Tile,player3Tile,player4Tile;
    private TextView turntxt;
    private ImageView voiceChatIndicator;

    private View view;
    private LinearLayout mainContainer, p3LinearLayout, p4LinearLayout;

    private boolean intialTurnUpdated = false;
    private boolean allOnline,allPlaying;

    private Connection connection;
    private RoomManager roomManager;
    private DatabaseReference roomRef,playerRef;
    private GameEngine gameEngine;
    private OnDisconnect online,playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chain_reaction);
        initialiseViews();
        setUpClickListeners();

        app = (App) getApplication();
        connection = Database.getConnection();

        String roomName = getIntent().getStringExtra("roomName");
        String[] players = getIntent().getStringArrayExtra("players");
        roomManager = new RoomManager(roomName);
        gameEngine = new GameEngine(roomName, new ArrayList<>(Arrays.asList(players)));
        initialiseGameEngine();
        roomRef = Database.getRoomRef(roomName);
        playerRef = roomRef.child("players").child(Auth.getUserName());
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

    @Override
    protected void onStop() {
        super.onStop();
        if(app.context==context){
            app.setContext(null);
        }
        roomManager.removeEventListener();
        gameEngine.removeListeners();
        connection.removeEventListener();
        playerRef.child("playing").setValue(false);
        if(app.call!=null){
            app.call.removeCallListener(callListener);
        }
    }

    //On User Input
    @Override
    public void onBackPressed() {
        ConfirmDialog confirmDialog = new ConfirmDialog(context,
                "Exit Game",
                "Do you want to exit the game?",
                () -> {
                    online.cancel();
                    playing.cancel();
                    roomManager.gameStarted(false);
                    roomManager.leftGame(Auth.getPlayer());
                    finish();
                },null);
        confirmDialog.show();
    }
    private void userClick(int row, int column){
        if(!gameEngine.isMyTurn() || gameEngine.isGameOver()){
            return;
        }
        int key = gameEngine.getKey(row,column);
        if(gameEngine.isCellFilled(key)){
            int playerNo = gameEngine.getPlayerNo(Auth.getUserName());
            Box box = gameEngine.getBox(key);
            if(box.getPlayerNo()==playerNo){
                Move move = new Move(key, Auth.getUserName());
                gameEngine.writeMove(move);
            }else{
                Toast.makeText(context,"Invalid Move",Toast.LENGTH_SHORT).show();
            }
        }else{
            Move move = new Move(key, Auth.getUserName());
            gameEngine.writeMove(move);
        }
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
    public void setUpEventListeners(){
        connection.setEventListener(connectionListener);
        roomManager.setEventListener(roomEventListener);
        gameEngine.setEventListener(gameEventListener);
    }
    GameEngine.EventListener gameEventListener = new GameEngine.EventListener(){
        @Override
        public void onGameResult(boolean won,String winner) {
            if(won){
                Snackbar.make(view,"You won the game",Snackbar.LENGTH_INDEFINITE).show();
            }else{
                Snackbar.make(view,winner+" won  the game",Snackbar.LENGTH_INDEFINITE).show();
            }
        }

        @Override
        public void onTurnChange(boolean userTurn,String userName) {
            if(userTurn){
                turntxt.setText("Your Turn");
            }else{
                turntxt.setText(userName+"'s Turn");
            }
        }
    };
    RoomManager.EventListener roomEventListener = new RoomManager.EventListener(){
        @Override
        public void onDataChange(Room room) {
            final ArrayList<String> players = new ArrayList<>();
            final int[] i = {2,0};
            allOnline=true;
            allPlaying=true;
            final Object[] opponentLeft = {false,""};
            final ArrayList<String> playerTileWise = new ArrayList<>();
            playerTileWise.add(Auth.getUserName());
            room.loopThroughPlayers((userName, player) -> {
                players.add(player.getUserName());
                i[1]++;
                if(!player.isOnline())
                    allOnline=false;
                if(!player.isPlaying())
                    allPlaying=false;
                if(Auth.isCurrentPlayer(player)) {
                    updatePlayerInfo(1,player);
                }else{
                    updatePlayerInfo(i[0], player);
                    playerTileWise.add(player.getUserName());
                    i[0]++;
                    if(player.isLeftGame()){
                        opponentLeft[0] =true;
                        opponentLeft[1] = player.getUserName();
                    }
                }
            });
            if(i[1]<3){
                p3LinearLayout.setVisibility(View.INVISIBLE);
                p4LinearLayout.setVisibility(View.INVISIBLE);
            }else if(i[1]==3){
                p3LinearLayout.setVisibility(View.VISIBLE);
                p4LinearLayout.setVisibility(View.INVISIBLE);
            }else if(i[1]==4){
                p3LinearLayout.setVisibility(View.VISIBLE);
                p4LinearLayout.setVisibility(View.VISIBLE);
            }
            playerTileWise.addAll(players);
            Collections.sort(players);
            for(int p=0;p<players.size();p++){
                String userName = players.get(p);
                int tileNo = playerTileWise.indexOf(userName)+1;
                updatePlayerColor(tileNo,p+1);
            }
            if(players.indexOf(Auth.getUserName())!=0 && !intialTurnUpdated){
                turntxt.setText(players.get(0)+"'s Turn");
                intialTurnUpdated=true;
            }
            if((boolean)opponentLeft[0]){
                String name = (String) opponentLeft[1];
                Toast.makeText(context,name+" Left the Game",Toast.LENGTH_SHORT).show();
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
    Connection.EventListener connectionListener = new Connection.EventListener(){
        @Override
        public void onConnected() {
            playerRef.child("online").setValue(true);
            playerRef.child("playing").setValue(true);
            online = playerRef.child("online").onDisconnect();
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

    //Update Players Information
    private void updatePlayerInfo(int tileNo, Player player){
        PlayerTile tile = getPlayerTile(tileNo);
        if(tile!=null){
            tile.setName(player.getName());
            tile.setDescription(player.getUserName());
            tile.setStatusIndicator(player.isOnline(),player.isPlaying());
            tile.setProfilePhoto(player.getPhotoUrl());
            if(allOnline && allPlaying)
                mainContainer.setBackgroundColor(getResources().getColor(R.color.green));
            else if(allOnline)
                mainContainer.setBackgroundColor(getResources().getColor(R.color.yellow));
            else
                mainContainer.setBackgroundColor(getResources().getColor(R.color.red));
        }
    }
    private void updatePlayerColor(int tileNo,int playerNo){
        PlayerTile tile = getPlayerTile(tileNo);
        if(tile!=null){
            tile.setNameColor(getPlayerColor(playerNo));
        }
    }
    private PlayerTile getPlayerTile(int tileNo){
        PlayerTile tile = null;
        if(tileNo==1)
            tile = player1Tile;
        else if(tileNo==2)
            tile = player2Tile;
        else if(tileNo==3)
            tile = player3Tile;
        else if(tileNo==4)
            tile = player4Tile;
        return tile;
    }
    private int getPlayerColor(int playerNo){
        if(playerNo==1)
            return getResources().getColor(R.color.blue);
        else if(playerNo==2)
            return getResources().getColor(R.color.green);
        else if(playerNo==3)
            return getResources().getColor(R.color.yellow);
        else if(playerNo==4)
            return getResources().getColor(R.color.red);
        return 0;
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
        player3Tile = new PlayerTile(
                findViewById(R.id.p3nametxt),
                findViewById(R.id.p3desctxt),
                findViewById(R.id.p3statusimg),
                findViewById(R.id.p3playerimg));
        player4Tile = new PlayerTile(
                findViewById(R.id.p4nametxt),
                findViewById(R.id.p4desctxt),
                findViewById(R.id.p4statusimg),
                findViewById(R.id.p4playerimg));
        p3LinearLayout = findViewById(R.id.player3Tile);
        p4LinearLayout = findViewById(R.id.player4Tile);
        box11 = findViewById(R.id.box11);
        box12 = findViewById(R.id.box12);
        box13 = findViewById(R.id.box13);
        box14 = findViewById(R.id.box14);
        box15 = findViewById(R.id.box15);
        box16 = findViewById(R.id.box16);
        box21 = findViewById(R.id.box21);
        box22 = findViewById(R.id.box22);
        box23 = findViewById(R.id.box23);
        box24 = findViewById(R.id.box24);
        box25 = findViewById(R.id.box25);
        box26 = findViewById(R.id.box26);
        box31 = findViewById(R.id.box31);
        box32 = findViewById(R.id.box32);
        box33 = findViewById(R.id.box33);
        box34 = findViewById(R.id.box34);
        box35 = findViewById(R.id.box35);
        box36 = findViewById(R.id.box36);
        box41 = findViewById(R.id.box41);
        box42 = findViewById(R.id.box42);
        box43 = findViewById(R.id.box43);
        box44 = findViewById(R.id.box44);
        box45 = findViewById(R.id.box45);
        box46 = findViewById(R.id.box46);
        box51 = findViewById(R.id.box51);
        box52 = findViewById(R.id.box52);
        box53 = findViewById(R.id.box53);
        box54 = findViewById(R.id.box54);
        box55 = findViewById(R.id.box55);
        box56 = findViewById(R.id.box56);
        box61 = findViewById(R.id.box61);
        box62 = findViewById(R.id.box62);
        box63 = findViewById(R.id.box63);
        box64 = findViewById(R.id.box64);
        box65 = findViewById(R.id.box65);
        box66 = findViewById(R.id.box66);
        box71 = findViewById(R.id.box71);
        box72 = findViewById(R.id.box72);
        box73 = findViewById(R.id.box73);
        box74 = findViewById(R.id.box74);
        box75 = findViewById(R.id.box75);
        box76 = findViewById(R.id.box76);
        box81 = findViewById(R.id.box81);
        box82 = findViewById(R.id.box82);
        box83 = findViewById(R.id.box83);
        box84 = findViewById(R.id.box84);
        box85 = findViewById(R.id.box85);
        box86 = findViewById(R.id.box86);
    }
    private void setUpClickListeners(){
        box11.setOnClickListener(view -> userClick(1,1));
        box12.setOnClickListener(view -> userClick(1,2));
        box13.setOnClickListener(view -> userClick(1,3));
        box14.setOnClickListener(view -> userClick(1,4));
        box15.setOnClickListener(view -> userClick(1,5));
        box16.setOnClickListener(view -> userClick(1,6));
        box21.setOnClickListener(view -> userClick(2,1));
        box22.setOnClickListener(view -> userClick(2,2));
        box23.setOnClickListener(view -> userClick(2,3));
        box24.setOnClickListener(view -> userClick(2,4));
        box25.setOnClickListener(view -> userClick(2,5));
        box26.setOnClickListener(view -> userClick(2,6));
        box31.setOnClickListener(view -> userClick(3,1));
        box32.setOnClickListener(view -> userClick(3,2));
        box33.setOnClickListener(view -> userClick(3,3));
        box34.setOnClickListener(view -> userClick(3,4));
        box35.setOnClickListener(view -> userClick(3,5));
        box36.setOnClickListener(view -> userClick(3,6));
        box41.setOnClickListener(view -> userClick(4,1));
        box42.setOnClickListener(view -> userClick(4,2));
        box43.setOnClickListener(view -> userClick(4,3));
        box44.setOnClickListener(view -> userClick(4,4));
        box45.setOnClickListener(view -> userClick(4,5));
        box46.setOnClickListener(view -> userClick(4,6));
        box51.setOnClickListener(view -> userClick(5,1));
        box52.setOnClickListener(view -> userClick(5,2));
        box53.setOnClickListener(view -> userClick(5,3));
        box54.setOnClickListener(view -> userClick(5,4));
        box55.setOnClickListener(view -> userClick(5,5));
        box56.setOnClickListener(view -> userClick(5,6));
        box61.setOnClickListener(view -> userClick(6,1));
        box62.setOnClickListener(view -> userClick(6,2));
        box63.setOnClickListener(view -> userClick(6,3));
        box64.setOnClickListener(view -> userClick(6,4));
        box65.setOnClickListener(view -> userClick(6,5));
        box66.setOnClickListener(view -> userClick(6,6));
        box71.setOnClickListener(view -> userClick(7,1));
        box72.setOnClickListener(view -> userClick(7,2));
        box73.setOnClickListener(view -> userClick(7,3));
        box74.setOnClickListener(view -> userClick(7,4));
        box75.setOnClickListener(view -> userClick(7,5));
        box76.setOnClickListener(view -> userClick(7,6));
        box81.setOnClickListener(view -> userClick(8,1));
        box82.setOnClickListener(view -> userClick(8,2));
        box83.setOnClickListener(view -> userClick(8,3));
        box84.setOnClickListener(view -> userClick(8,4));
        box85.setOnClickListener(view -> userClick(8,5));
        box86.setOnClickListener(view -> userClick(8,6));
    }
    private void initialiseGameEngine(){
        for(int row=1 ; row<9 ; row++){
            for(int column=1 ; column<7 ; column++){
                gameEngine.createBox(row,column,getBoxImageView(row,column));
            }
        }
    }

    //Get ImageView of Specific Box
    private ImageView getBoxImageView(int row, int column){
        if(row==1 && column==1) return box11;
        if(row==1 && column==2) return box12;
        if(row==1 && column==3) return box13;
        if(row==1 && column==4) return box14;
        if(row==1 && column==5) return box15;
        if(row==1 && column==6) return box16;
        if(row==2 && column==1) return box21;
        if(row==2 && column==2) return box22;
        if(row==2 && column==3) return box23;
        if(row==2 && column==4) return box24;
        if(row==2 && column==5) return box25;
        if(row==2 && column==6) return box26;
        if(row==3 && column==1) return box31;
        if(row==3 && column==2) return box32;
        if(row==3 && column==3) return box33;
        if(row==3 && column==4) return box34;
        if(row==3 && column==5) return box35;
        if(row==3 && column==6) return box36;
        if(row==4 && column==1) return box41;
        if(row==4 && column==2) return box42;
        if(row==4 && column==3) return box43;
        if(row==4 && column==4) return box44;
        if(row==4 && column==5) return box45;
        if(row==4 && column==6) return box46;
        if(row==5 && column==1) return box51;
        if(row==5 && column==2) return box52;
        if(row==5 && column==3) return box53;
        if(row==5 && column==4) return box54;
        if(row==5 && column==5) return box55;
        if(row==5 && column==6) return box56;
        if(row==6 && column==1) return box61;
        if(row==6 && column==2) return box62;
        if(row==6 && column==3) return box63;
        if(row==6 && column==4) return box64;
        if(row==6 && column==5) return box65;
        if(row==6 && column==6) return box66;
        if(row==7 && column==1) return box71;
        if(row==7 && column==2) return box72;
        if(row==7 && column==3) return box73;
        if(row==7 && column==4) return box74;
        if(row==7 && column==5) return box75;
        if(row==7 && column==6) return box76;
        if(row==8 && column==1) return box81;
        if(row==8 && column==2) return box82;
        if(row==8 && column==3) return box83;
        if(row==8 && column==4) return box84;
        if(row==8 && column==5) return box85;
        if(row==8 && column==6) return box86;
        return null;
    }

}
