package com.mpgames.zone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mpgames.zone.userlist.User;

public class AddFriendActivity extends AppCompatActivity {
    private final String TAG = "AddFriendActivity";
    private final Context context = AddFriendActivity.this;
    private App app;
    private EditText usernameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        usernameEditText = findViewById(R.id.usernametxt);
        app = (App) getApplication();
    }
    public void addClick(View v){
        String email = usernameEditText.getText().toString();
        if(email==null || email.isEmpty()) return;
        String username;
        if(email.contains("@gmail.com")){
            username = email.substring(0,email.indexOf('@'));
        }else{
            username = email.substring(0,email.indexOf('@'))+"_" +email.substring(email.indexOf("@"),email.lastIndexOf("."));
        }
        tryUsername(username);
    }
    private void tryUsername(String username){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Adding "+username+" in your friend list...");
        progressDialog.show();
        Database.getUserRef(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User user = App.getUser();
                    int x = 0;
                    if(user.getFriends()!=null)
                        x = user.getFriends().size();
                    Database.getUserRef().child("friends").child(""+x).setValue(username)
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(context,username+" has been added to your friends.",Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(context,"Couldn't add "+username+" as your friend.",Toast.LENGTH_SHORT).show();
                            });
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(context,"User : "+username+" not found.",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(context,"Couldn't connect to database, check your internet.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.context==context){
            app.setContext(null);
        }
    }
}
