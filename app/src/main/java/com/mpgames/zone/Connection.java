package com.mpgames.zone;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class Connection {
    private String TAG = "Connection";
    private EventListener eventListener;
    private DatabaseReference connectionRef;
    public Connection() {
        connectionRef = Database.getConnectionRef();
    }

    public void removeEventListener(){
        setEventListener(null);
        connectionRef.removeEventListener(connectionEventListener);
    }

    public interface EventListener {
        void onConnected();
        void onDisconnected();
    }

    public void setEventListener(EventListener eventListener){
        this.eventListener = eventListener;
        connectionRef.addValueEventListener(connectionEventListener);
    }

    private ValueEventListener connectionEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(eventListener !=null){
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    eventListener.onConnected();
                } else {
                    eventListener.onDisconnected();
                }
            }else{
                connectionRef.removeEventListener(connectionEventListener);
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG,"Listener was cancelled");
        }
    };

}
