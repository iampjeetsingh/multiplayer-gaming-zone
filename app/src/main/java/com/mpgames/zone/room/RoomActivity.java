package com.mpgames.zone.room;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.OnDisconnect;
import com.mpgames.zone.Actions;
import com.mpgames.zone.App;
import com.mpgames.zone.Auth;
import com.mpgames.zone.Connection;
import com.mpgames.zone.Database;
import com.mpgames.zone.Game;
import com.mpgames.zone.PermissionsActivity;
import com.mpgames.zone.invite.InviteFragment;
import com.mpgames.zone.R;
import com.mpgames.zone.dialog.ConfirmDialog;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;
import java.util.ArrayList;
import java.util.List;

public class RoomActivity extends AppCompatActivity {
    private String TAG="RoomActivity";
    private TextView leadertxt,gametxt;
    private ImageView voicechatimg;
    private RoomManager roomManager;
    private Connection connection;
    private Context context = RoomActivity.this;
    private boolean ready=false,allReady=false,allOnline=false;
    private boolean leader = false;
    private FirebaseRecyclerAdapter<Player, ViewHolder> firebaseRecyclerAdapter;
    private RecyclerView recyclerView;
    private Button readyButton;
    private DatabaseReference playersRef,playerRef,roomRef;
    private OnDisconnect onlineRefOnDisconnect;
    private boolean leftRoom=false,gameStarted=false;
    private Room room;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        recyclerView = findViewById(R.id.recyclerView);
        gametxt = findViewById(R.id.gametxt);
        leadertxt = findViewById(R.id.leadertxt);
        voicechatimg = findViewById(R.id.voicechatimg);
        readyButton = findViewById(R.id.readyButton);

        app = (App) getApplication();
        connection = Database.getConnection();

        final String roomName = getIntent().getStringExtra("roomName");
        if(roomName==null){
            finish();
            return;
        }

        roomManager = new RoomManager(roomName);
        leadertxt.setText(roomName);
        if(roomName.equals(Auth.getUserName())){
            leader = true;
            findViewById(R.id.startbtn).setVisibility(View.VISIBLE);
        }
        roomRef = roomManager.getReference();
        playersRef = roomRef.child("players");
        playerRef = playersRef.child(Auth.getUserName());
        onlineRefOnDisconnect = playerRef.child("online").onDisconnect();
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLinearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
        setUpEventListeners();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Player, ViewHolder>(Player.class,R.layout.player_row, ViewHolder.class,playersRef) {
            @Override
            protected void populateViewHolder(ViewHolder viewHolder, Player player, int i) {
                viewHolder.setImage(player.getPhotoUrl());
                viewHolder.setName(player.getName());
                viewHolder.setUserName(player.getUserName());
                viewHolder.setStatus(player.isOnline(),player.isPlaying(),player.isReady(),player.isVoiceChatOn());
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        if(app.checkPermissions()){
            if(app.call!=null){
                app.call.addCallListener(callListener);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.context==context){
            app.setContext(null);
        }
        removeEventListeners();
        if(!leftRoom && !gameStarted){
            playerRef.child("online").setValue(false);
        }
        if(leftRoom || !gameStarted){
            if(app.call!=null){
                app.call.hangup();
            }
        }
    }

    //Setting Event Listeners
    private void setUpEventListeners(){
        roomManager.setEventListener(eventListener);
        connection.setEventListener(connectionListener);
    }
    private void removeEventListeners(){
        roomManager.removeEventListener();
        connection.removeEventListener();
    }
    RoomManager.EventListener eventListener = new RoomManager.EventListener() {
        @Override
        public void onDataChange(Room room) {
            RoomActivity.this.room = room;
            gametxt.setText(room.getData().getGameName());
            if(room.getData().isGameStarted()){
                final ArrayList<String> players = new ArrayList<>();
                room.loopThroughPlayers((userName, player) -> {
                    players.add(player.getUserName());
                });
                gameStarted=true;
                roomManager.removeEventListener();
                String gameName = room.getData().getGameName();
                Actions.startGame(Game.withName(gameName),context,roomManager.getRoomName(),players);
            }
            allReady = true;
            allOnline=true;
            room.loopThroughPlayers((userName, player) -> {
                if(!player.isReady()){
                    allReady = false;
                    if(Auth.isCurrentPlayer(player)){
                        ready = false;
                        readyButton.setText("Ready");
                    }
                }else {
                    if(Auth.isCurrentPlayer(player)){
                        ready = true;
                        readyButton.setText("Not Ready");
                    }
                }
                if(!player.isOnline()){
                    allOnline=false;
                }
            });
        }
        @Override
        public void onRoomDeleted() {
            leftRoom=true;
            onlineRefOnDisconnect.cancel();
            removeEventListeners();
            Database.updateCurrentRoom(null);
            roomManager.deleteRoom();
            Toast.makeText(context,"Leader left the Room",Toast.LENGTH_SHORT).show();
            finish();
        }
    };
    Connection.EventListener connectionListener = new Connection.EventListener() {
        @Override
        public void onConnected() {
            playerRef.child("online").setValue(true);
            onlineRefOnDisconnect.removeValue();
        }

        @Override
        public void onDisconnected() {
        }
    };
    CallListener callListener = new CallListener() {
        @Override
        public void onCallProgressing(Call call) {
            voicechatimg.setImageResource(R.drawable.ic_mic_none);
        }
        @Override
        public void onCallEstablished(Call call) {
            roomManager.joinedVoiceChat(Auth.getPlayer());
            Toast.makeText(context,"Voice Chat Connected",Toast.LENGTH_SHORT).show();
            voicechatimg.setImageResource(R.drawable.ic_mic);
        }
        @Override
        public void onCallEnded(Call call) {
            app.call.removeCallListener(callListener);
            app.call = null;
            roomManager.leftVoiceChat(Auth.getPlayer());
            Toast.makeText(context,"Voice Chat Ended",Toast.LENGTH_SHORT).show();
            voicechatimg.setImageResource(R.drawable.ic_mic_off);
        }
        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {
        }
    };

    //Handling User Clicks
    @Override
    public void onBackPressed() {
        exitClick(null);
    }
    public void exitClick(View v){
        roomManager.removeEventListener();
        ConfirmDialog confirmDialog = new ConfirmDialog(context,
                "Leave Room",
                "Are you sure?",
                new Runnable() {
                    @Override
                    public void run() {
                        if(leader){
                            roomManager.removePlayer(Auth.getPlayer());
                            roomManager.deleteRoom();
                        }else{
                            roomManager.removePlayer(Auth.getPlayer());
                        }
                        onlineRefOnDisconnect.cancel();
                        leftRoom=true;
                        finish();
                    }
                },null);
        confirmDialog.show();
    }
    public void readyClick(View v){
        if(!ready){
            ready = true;
            roomManager.ready(Auth.getPlayer());
            Button button = (Button) v;
            button.setText("Not Ready");
        }else{
            ready = false;
            roomManager.unready(Auth.getPlayer());
            Button button = (Button) v;
            button.setText("Ready");
        }
    }
    public void inviteClick(View v){
        InviteFragment dialogFragment = new InviteFragment();
        dialogFragment.show(getSupportFragmentManager(),"invite");
    }
    public void startClick(View v){
        if(allOnline){
            if(allReady){
                String gameName = gametxt.getText().toString();
                Database.getGameRef(gameName, roomManager.getRoomName()).removeValue();
                final int[] playerCount = {0};
                room.loopThroughPlayers((userName, player) -> {
                    roomManager.joinedGame(player);
                    roomManager.unready(player);
                    playerCount[0]++;
                });
                int maxPlayers = Game.getMaxPlayers(Game.withName(gameName));
                if(maxPlayers>=playerCount[0])
                    roomManager.gameStarted(true);
                else
                    Toast.makeText(context,"Only "+maxPlayers+" players can play "+gameName,Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(context,"All Players are not Ready",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(context,"All Players are not Online",Toast.LENGTH_SHORT).show();
        }
    }
    public void gameClick(View v){
        if(leader){
            PopupMenu popup = new PopupMenu(context, v);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if(id==R.id.chess){
                        roomManager.updateGame("Chess");
                        return true;
                    }else if(id==R.id.chainreaction){
                        roomManager.updateGame("ChainReaction");
                        return true;
                    }else if(id==R.id.tictactoe){
                        roomManager.updateGame("TicTacToe");
                        return true;
                    }else if(id==R.id.teenpatti){
                        roomManager.updateGame("TeenPatti");
                        return true;
                    }
                    return false;
                }
            });
            popup.inflate(R.menu.games);
            popup.show();
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
}
