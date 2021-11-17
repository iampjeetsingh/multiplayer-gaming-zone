package com.mpgames.zone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {
    private Context context = AccountActivity.this;
    private String TAG = "AccountActivity";
    private App app;

    private int GALLERY_INTENT=7;

    private Dialog dialog;
    private  ProgressDialog progressDialog;

    private StorageReference reference;
    private Bitmap bitmap;

    private Uri photoUri;
    private String name;

    private ImageView imageView;
    private EditText editText;


    private ImageView profileImageView;
    private TextView nameTextView,emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailtxt);
        profileImageView = findViewById(R.id.profilepic);
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        reference = FirebaseStorage.getInstance().getReference().child("Profile_Photos").child(Auth.getUserName());
        app = (App) getApplication();
    }
    public void logOut(View v){
        startActivity(new Intent(context,LoginActivity.class).putExtra("signOut",true));
        finish();
    }
    public void deleteAccount(View v){
        startActivity(new Intent(context,LoginActivity.class).putExtra("deleteAccount",true));
        finish();
    }
    public void editClick(View v){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_account);
        dialog.setCancelable(false);
        EditText editText = dialog.findViewById(R.id.editText);
        ImageView imageView = dialog.findViewById(R.id.imageView);
        editText.setText(Auth.getDisplayName());
        this.imageView = imageView;
        this.editText = editText;
        if(Auth.getPhotoUrl()!=null){
            Glide.with(context)
                    .load(Auth.getPhotoUrl())
                    .into(imageView);
        }
        Button updateButton = dialog.findViewById(R.id.updateButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        imageView.setOnClickListener(imageClickListener);
        updateButton.setOnClickListener(updateClickListener);
        cancelButton.setOnClickListener(cancelClickListener);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    View.OnClickListener imageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            callGalary();
        }
    };

    View.OnClickListener updateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            name = editText.getText().toString();
            if(name.equals("")){
                name = Auth.getDisplayName();
            }
            if(bitmap==null){
                photoUri = Auth.getUser().getPhotoUrl();
                progressDialog.setMessage("Updating Profile...");
                progressDialog.show();
                updateData();
            }else{
                progressDialog.setMessage("Uploading Photo...");
                progressDialog.show();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bytes = baos.toByteArray();
                final UploadTask uploadTask = reference.putBytes(bytes);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return reference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            photoUri = downloadUri;
                            Toast.makeText(context,"Photo Uploaded",Toast.LENGTH_SHORT).show();
                            progressDialog.setMessage("Updating Profile...");
                            updateData();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    };
    private void updateData(){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(photoUri)
                .setDisplayName(name)
                .build();
        Auth.getUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Map<String,Object> map = new HashMap<>();
                    map.put("name",name);
                    map.put("photoUrl",photoUri.toString());
                    Database.getUserRef().updateChildren(map);
                    progressDialog.dismiss();
                    dialog.dismiss();
                    Toast.makeText(context,"Profile Updated",Toast.LENGTH_SHORT).show();
                    refresh();
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.dismiss();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
        refresh();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.context==context){
            app.setContext(null);
        }
    }

    private void refresh(){
        emailTextView.setText(Auth.getEmail());
        nameTextView.setText(Auth.getDisplayName());
        String url = Auth.getPhotoUrl();
        if(!(url==null)){
            Glide.with(this)
                    .load(Auth.getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImageView);
        }
    }

    private void callGalary() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        try {
            intent.putExtra("return-data", true);
            startActivityForResult(intent, GALLERY_INTENT);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG,""+e.getMessage());
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                bitmap = extras.getParcelable("data");
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
