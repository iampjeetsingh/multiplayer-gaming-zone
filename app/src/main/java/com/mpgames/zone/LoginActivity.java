package com.mpgames.zone;

import androidx.annotation.NonNull;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mpgames.zone.room.Player;
import com.mpgames.zone.userlist.UsersActivity;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity{
    private String TAG = "LoginActivity";
    private SignInButton signInButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 1;
    private Context context = LoginActivity.this;
    private ProgressDialog progressDialog;
    private boolean deleteAccount;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        app = (App) getApplication();
        signInButton = findViewById(R.id.signInBtn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Signing In...");
        boolean signOut = getIntent().getBooleanExtra("signOut",false);
        deleteAccount = getIntent().getBooleanExtra("deleteAccount",false);
        if(signOut){
            signOut();
        }
        if(deleteAccount){
            Toast.makeText(context,"To delete your account, sign in again to confirm its you",Toast.LENGTH_SHORT).show();
            signOut();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
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

    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("330144008329-q1leeldfg5k7sgtq6vnrf6t48tir56ln.apps.googleusercontent.com")
            .requestEmail()
            .build();

    private void signIn() {
        progressDialog.show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!deleteAccount)
                            Toast.makeText(context,"Successfully logged out",Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        mAuth.signOut();
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                progressDialog.hide();
                Toast.makeText(context,"Something Went Wrong",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        progressDialog.hide();
                        Toast.makeText(context,"Something Went Wrong",Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(final FirebaseUser user) {
        if (user != null) {
            if(deleteAccount){
                deleteAccount = false;
                App app = (App)getApplication();
                app.removeListeners();
                Database.getUserRef().removeValue();
                Database.getInviteRef().removeValue();
                Database.getRoomRef(Auth.getUserName()).removeValue();
                StorageReference reference = FirebaseStorage.getInstance().getReference().child("Profile_Photos").child(Auth.getUid());
                reference.delete();
                user.delete().addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        Toast.makeText(context,"Account Successfully Deleted",Toast.LENGTH_SHORT).show();
                        Log.e(TAG,""+Auth.getUser());
                    }else{
                        Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            final DatabaseReference reference = Database.getUserRef();
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && dataSnapshot.hasChild("userName")){
                        progressDialog.dismiss();
                        Intent intent = new Intent(context, UsersActivity.class);
                        startActivity(intent);
                    }else{
                        Player player = Auth.getPlayer();
                        player.setOnline(true);
                        reference.setValue(player).addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if(task.isSuccessful()){
                                Intent intent = new Intent(context, UsersActivity.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(context,"Something Went Wrong",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressDialog.dismiss();
                    Log.e(TAG,"Listener was cancelled");
                }
            });
        }
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
