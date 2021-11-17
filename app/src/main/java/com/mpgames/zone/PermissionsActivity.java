package com.mpgames.zone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.util.List;

public class PermissionsActivity extends AppCompatActivity {
    Context context = PermissionsActivity.this;
    App app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        app = (App) getApplication();
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.context==context)
            app.setContext(null);
    }

    public void grantClick(View v){
        List<String> permissionsNeeded = app.getPermissionsNeeded();
        ActivityCompat.requestPermissions(PermissionsActivity.this,
                permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                8);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==8){
            if(app.checkPermissions()){
                setResult(RESULT_OK);
                finish();
            }else{
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    public void disableClick(View v){
        setResult(RESULT_CANCELED);
        finish();
    }
}
