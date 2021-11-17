package com.mpgames.zone.invite;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mpgames.zone.App;
import com.mpgames.zone.Auth;
import com.mpgames.zone.Database;
import com.mpgames.zone.room.Player;
import com.mpgames.zone.userlist.User;
import com.mpgames.zone.room.RoomManager;

public class Invite {
    private String senderName,senderUserName,senderPhotoUrl,senderUserID,roomName;
    private String receiver;

    public Invite() {
    }

    public void setSender(Player player){
        this.senderName = player.getName();
        this.senderUserName = player.getUserName();
        this.senderPhotoUrl = player.getPhotoUrl();
        this.senderUserID = player.getUserID();
    }

    public void sentTo(User user){
        Player sender = Auth.getPlayer();
        setSender(sender);
        User currentUser = App.getUser();
        if(currentUser.isInRoom()){
            setRoomName(currentUser.getCurrentRoomName());
        }else{
            setRoomName(sender.getUserName());
            RoomManager roomManager = new RoomManager(getRoomName());
            roomManager.deleteRoom();
            roomManager.createRoom();
        }
        String userName = user.getUserName();
        Database.getInviteRef(userName).removeValue();
        Database.getInviteRef(userName).setValue(Invite.this);
        receiver = userName;
    }

    public void accept(){
        Database.getInviteRef().child("accepted").setValue(true);
        RoomManager roomManager = new RoomManager(roomName);
        roomManager.addPlayer(Auth.getPlayer());
    }

    public void reject(){
        Database.getInviteRef().removeValue();
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderPhotoUrl() {
        return senderPhotoUrl;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public interface ResponseListener {
        void onInviteAccepted();
        void onInviteRejected();
    }
    ValueEventListener inviteEventListener;
    public void setResponseListener(final ResponseListener responseListener){
        final DatabaseReference inviteRef = Database.getInviteRef(receiver);
        inviteEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    inviteRef.removeEventListener(inviteEventListener);
                    responseListener.onInviteRejected();
                }else if(dataSnapshot.exists() && dataSnapshot.hasChild("accepted")){
                    inviteRef.removeEventListener(inviteEventListener);
                    responseListener.onInviteAccepted();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Invite","Invite Listener was cancelled");
            }
        };
        inviteRef.addValueEventListener(inviteEventListener);

    }
}
