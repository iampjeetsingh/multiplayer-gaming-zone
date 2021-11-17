package com.mpgames.zone.userlist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.mpgames.zone.AccountActivity;
import com.mpgames.zone.AddFriendActivity;
import com.mpgames.zone.App;
import com.mpgames.zone.Auth;
import com.mpgames.zone.Database;
import com.mpgames.zone.R;
import com.mpgames.zone.dialog.ConfirmDialog;
import com.mpgames.zone.invite.Invite;
import com.mpgames.zone.room.RoomActivity;
import com.mpgames.zone.room.RoomManager;

import java.util.ArrayList;

public class UsersActivity extends Activity {
    private final Context context = UsersActivity.this;
    private App app;
    private RecyclerView recyclerView;
    private DatabaseReference reference;
    private FirebaseRecyclerAdapter<User, ViewHolder> firebaseRecyclerAdapter;
    private final ArrayList<User> usersToDisplay = new ArrayList<>();
    private Toolbar toolbar;
    private ImageView accountimg;
    private User appuser;
    private LinearLayout msglayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        recyclerView = findViewById(R.id.recyclerView);
        msglayout = findViewById(R.id.msglayout);
        app = (App) getApplication();

        accountimg = findViewById(R.id.accountimg);
        toolbar = findViewById(R.id.toolbar);
        appuser = app.getUser();
        if(appuser !=null && appuser.isInRoom() && appuser.getCurrentRoomName()!=null){
            showDialog(appuser.getCurrentRoomName());
        }
        reference = Database.getUsersRef();
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(usersAdapter);
        app.setProfileConnectionListener(() -> {
            appuser = app.getUser();
            refresh();
        });
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener((v)->{
            Intent intent = new Intent(context, AddFriendActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        app.setContext(context);
        initialise();
        app.setFriendsListener(this::refresh);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refresh(){
        usersToDisplay.clear();
        usersToDisplay.addAll(app.getFriends());
        usersAdapter.notifyDataSetChanged();
        if(usersToDisplay.isEmpty()){
            msglayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            msglayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.context==context){
            app.setContext(null);
        }
    }

    private final UsersAdapter usersAdapter = new UsersAdapter(usersToDisplay) {
        @Override
        public void populateViewHolder(ViewHolder viewHolder, User user, int i) {
            viewHolder.setName(user.getName());
            viewHolder.setUserName(user.getUserName());
            viewHolder.setDescription("last seen "+ user.getLastSeen());
            viewHolder.setImage(user.getPhotoUrl());
            viewHolder.setStatusIndicator(user.isOnline(),user.isInRoom());
            final Button invitebtn = viewHolder.getInviteButton();
            invitebtn.setOnClickListener(view -> {
                Invite invite = new Invite();
                invite.sentTo(user);
                invitebtn.setEnabled(false);
                invite.setResponseListener(new Invite.ResponseListener() {
                    @Override
                    public void onInviteAccepted() {
                        invitebtn.setEnabled(true);
                        Database.getInviteRef(user.getUserName()).removeValue();
                        Toast.makeText(context,"Invite Accepted!!!",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, RoomActivity.class);
                        intent.putExtra("roomName", Auth.getUserName());
                        startActivity(intent);
                    }
                    @Override
                    public void onInviteRejected() {
                        invitebtn.setEnabled(true);
                        Toast.makeText(context,"Invite Rejected!!!", Toast.LENGTH_SHORT).show();
                        User user1 = app.getUser();
                        RoomManager roomManager = new RoomManager(user1.getCurrentRoomName());
                        roomManager.deleteRoom();
                        Database.updateCurrentRoom(null);
                    }
                });
            });
        }
    };

    private void showDialog(final String roomName){
        ConfirmDialog confirmDialog = new ConfirmDialog(context,
                "Resume Game",
                "Do you want to resume your previous game?",
                () -> {
                    Intent intent = new Intent(context, RoomActivity.class);
                    intent.putExtra("roomName",roomName);
                    startActivity(intent);
                },
                () -> {
                    Database.getUserRef().child("inRoom").removeValue();
                    Database.getUserRef().child("currentRoomName").removeValue();
                    RoomManager roomManager = new RoomManager(roomName);
                    if(Auth.getUserName().equals(roomName)){
                        roomManager.deleteRoom();
                        Database.updateCurrentRoom(null);
                    }else{
                        roomManager.removePlayer(Auth.getPlayer());
                    }
                }
        );
        confirmDialog.show();
    }

    private void initialise(){
        String photoUrl = Auth.getPhotoUrl();
        if(photoUrl!=null){
            Glide.with(context)
                    .load(photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(accountimg);
        }
        accountimg.setOnClickListener(view -> startActivity(new Intent(context, AccountActivity.class)));
        toolbar.setTitle("Friends");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }


}
