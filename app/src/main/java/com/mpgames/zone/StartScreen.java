package com.mpgames.zone;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mpgames.zone.room.Player;
import com.mpgames.zone.userlist.UsersActivity;
import com.mpgames.zone.userlist.User;

public class StartScreen extends AppCompatActivity {
    private String TAG="StartScreen";
    private Context context = StartScreen.this;
    private App app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        app = (App) getApplication();
        if(Auth.userIsNull()){
            Intent intent = new Intent(context,LoginActivity.class);
            startActivity(intent);
        }else{
            final App app = (App) getApplication();
            app.setProfileConnectionListener(new App.ProfileConnectionListener() {
                @Override
                public void onConnected() {
                    app.removeProfileConnectionListener();
                    User user = App.getUser();
                    if(user!=null){
                        startApp();
                    }else{
                        Player player = Auth.getPlayer();
                        player.setOnline(true);
                        Database.getUserRef().setValue(player);
                    }
                }
            });
        }
    }

    private void startApp(){
        Intent intent = new Intent(context, UsersActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.setContext(context);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(app.context==context){
            app.setContext(null);
        }
    }
}
