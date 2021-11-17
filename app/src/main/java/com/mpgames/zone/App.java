package com.mpgames.zone;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mpgames.zone.invite.Invite;
import com.mpgames.zone.invite.InviteDialog;
import com.mpgames.zone.userlist.User;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class App extends MultiDexApplication {
    private String TAG = "App";
    public Context context;

    private static User user;
    private boolean connected;
    private Connection connection;

    private ProfileConnectionListener profileConnectionListener;
    private boolean profileConnected = false;
    private final static HashMap<String, User> friends = new HashMap<>();
    private final TreeSet<String> friendListeners = new TreeSet<>();

    DatabaseReference usersRef,inviteRef;
    FirebaseAuth.AuthStateListener authStateListener;

    public SinchClient sinchClient;
    public CallClient callClient;
    public Call call;

    String[] permissions= new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.READ_PHONE_STATE};

    @Override
    public void onCreate() {
        super.onCreate();
        if(Auth.userIsNotNull()){
            onStart();
        }else{
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(Auth.userIsNotNull()){
                        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
                        onStart();
                    }
                }
            };
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
        }
    }
    public void onStart(){
        usersRef = Database.getUserRef();
        inviteRef = Database.getInviteRef();
        inviteRef.onDisconnect().removeValue();
        connection = new Connection();
        setUpEventListeners();
        setUpSinchClient();
    }


    private void setUpSinchClient(){
        String APP_KEY = BuildConfig.APP_KEY;
        String APP_SECRET = BuildConfig.APP_SECRET;
        String ENVIRONMENT = BuildConfig.ENVIRONMENT;
        sinchClient = Sinch.getSinchClientBuilder().context(getApplicationContext())
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .userId(Auth.getUid())
                .build();
        sinchClient.setSupportCalling(true);
        sinchClient.addSinchClientListener(new SinchClientListener() {
            public void onClientStarted(SinchClient client) { }
            public void onClientStopped(SinchClient client) { }
            public void onClientFailed(SinchClient client, SinchError error) { }
            public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration registrationCallback) { }
            public void onLogMessage(int level, String area, String message) {
                Log.println(level,area,message);
            }
        });
        callClient = sinchClient.getCallClient();
        sinchClient.start();
    }

    private final ValueEventListener friendListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                User user = dataSnapshot.getValue(User.class);
                if(user!=null && user.getUserName()!=null){
                    friends.put(user.getUserName(), user);
                    if(friendsListener!=null)
                        friendsListener.onFriendsFetched();
                }
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };
    private void setUpEventListeners(){
        connection.setEventListener(connectionEventListener);
        inviteRef.addValueEventListener(inviteEventListener);
        usersRef.addValueEventListener(userEventListener);
    }
    ValueEventListener inviteEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists() && !dataSnapshot.hasChild("accepted")){
                Invite invite = dataSnapshot.getValue(Invite.class);
                InviteDialog inviteDialog = new InviteDialog(invite,context);
                inviteDialog.show();
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.w(TAG, "Invite Listener was cancelled");
        }
    };
    ValueEventListener userEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            user = dataSnapshot.getValue(User.class);
            if(profileConnectionListener !=null){
                profileConnectionListener.onConnected();
                profileConnected=true;
            }
            if(user!=null && user.getFriends()!=null){
                for(String friendUsername : user.getFriends()){
                    if(friendUsername!=null){
                        if(!friendListeners.contains(friendUsername)){
                            friendListeners.add(friendUsername);
                            Database.getUserRef(friendUsername).addValueEventListener(friendListener);
                        }
                    }
                }
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.w(TAG, "Auth Listener was cancelled");
        }
    };
    Connection.EventListener connectionEventListener = new Connection.EventListener() {
        @Override
        public void onConnected() {
            connected = true;
            usersRef.child("online").setValue(true);
            usersRef.child("online").onDisconnect().setValue(false);
        }
        @Override
        public void onDisconnected() {
            connected = false;
        }
    };
    public void removeListeners(){
        inviteRef.removeEventListener(inviteEventListener);
        usersRef.removeEventListener(userEventListener);
        connection.removeEventListener();
    }


    public Context getContext() {
        return context;
    }
    public void setContext(Context context) {
        boolean contextNull;
        Context previousContext = this.context;
        if(this.context==context){
            return;
        }
        this.context = context;
        contextNull = context == null;
        if(previousContext!=null && context!=null){
            return;
        }
        if(contextNull){
            appWentInBackground();
        }else{
            appCameInForeground();
        }
    }


    public boolean checkPermissions() {
        int result;
        List<String> permissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(context,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(p);
            }
        }
        return permissionsNeeded.isEmpty();
    }
    public List<String> getPermissionsNeeded(){
        int result;
        List<String> permissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(context,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(p);
            }
        }
        return permissionsNeeded;
    }


    public static User getUser(){
        return user;
    }


    private void appWentInBackground(){
        if(connected && Auth.userIsNotNull()){
            DatabaseReference ref = Database.getUserRef();
            ref.child("online").setValue(false);
            ref.child("lastSeen").setValue(ServerValue.TIMESTAMP);
            if(call!=null){
                call.hangup();
                call=null;
            }
        }
    }
    private void appCameInForeground(){
        if(connected && Auth.userIsNotNull()){
            DatabaseReference ref = Database.getUserRef();
            ref.child("online").setValue(true);
        }
    }

    public static Collection<User> getFriends(){
        return friends.values();
    }


    public interface ProfileConnectionListener {
        void onConnected();
    }

    public void setProfileConnectionListener(ProfileConnectionListener profileConnectionListener) {
        this.profileConnectionListener = profileConnectionListener;
        if(profileConnected){
            this.profileConnectionListener.onConnected();
        }
    }
    public void removeProfileConnectionListener(){
        this.profileConnectionListener = null;
    }

    public interface FriendsListener{
        void onFriendsFetched();
    }
    private static FriendsListener friendsListener;
    public static void setFriendsListener(FriendsListener friendsListener) {
        App.friendsListener = friendsListener;
        if(friendsListener!=null)
            friendsListener.onFriendsFetched();
    }
}
