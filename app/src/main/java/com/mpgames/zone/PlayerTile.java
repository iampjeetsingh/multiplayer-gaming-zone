package com.mpgames.zone;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

public class PlayerTile{
    private TextView nameTextView, descriptionTextView;
    private ImageView statusIndicator,profilePhoto;
    public PlayerTile(View nameTextView, View descriptionTextView, View statusIndicator, View profilePhoto) {
        this.nameTextView = (TextView) nameTextView;
        this.descriptionTextView = (TextView) descriptionTextView;
        this.statusIndicator = (ImageView) statusIndicator;
        this.profilePhoto = (ImageView) profilePhoto;
    }
    public void setName(String name){
        nameTextView.setText(name);
    }
    public void setNameColor(int color){
        nameTextView.setTextColor(color);
    }
    public void setDescription(String description){
        descriptionTextView.setText(description);
    }
    public void setStatusIndicator(boolean online,boolean playing){
        if(online && playing)
            statusIndicator.setImageResource(R.drawable.green_dot);
        else if(online)
            statusIndicator.setImageResource(R.drawable.yellow_dot);
        else
            statusIndicator.setImageResource(R.drawable.red_dot);
    }
    public void setProfilePhoto(String photoUrl){
        if(photoUrl!=null){
            Glide.with(profilePhoto.getContext())
                    .load(photoUrl)
                    .into(profilePhoto);
        }
    }
}