package com.mpgames.zone.room;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.mpgames.zone.R;

public class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView nameTextView, userNameTextView,readyIndicator;
    private final ImageView playerImageView, onlineStatusImageView,voiceChatStatusImageView;
    private final LinearLayout layout;
    final LinearLayout.LayoutParams params;

    public ViewHolder(final View itemView) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        userNameTextView = itemView.findViewById(R.id.usernametxt);
        playerImageView = itemView.findViewById(R.id.playerimg);
        onlineStatusImageView = itemView.findViewById(R.id.statusimg);
        voiceChatStatusImageView = itemView.findViewById(R.id.voicechatindicator);
        readyIndicator = itemView.findViewById(R.id.readyIndicator);
        layout = itemView.findViewById(R.id.chat_layout);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setName(String name) {
        nameTextView.setText(name);
    }

    public void setStatus(boolean online,boolean playing,boolean ready,boolean voiceChatOn){
        setStatusIndicator(online,playing);
        setReadyIndicator(ready);
        setVoiceChatIndicator(voiceChatOn,online);
    }

    private void setStatusIndicator(boolean online,boolean playing){
        onlineStatusImageView.setVisibility(View.VISIBLE);
        if(online){
            if(playing){
                onlineStatusImageView.setImageResource(R.drawable.yellow_dot);
            }else{
                onlineStatusImageView.setImageResource(R.drawable.green_dot);
            }
        }else{
            onlineStatusImageView.setImageResource(R.drawable.red_dot);
        }
    }

    private void setReadyIndicator(boolean ready){
        readyIndicator.setVisibility(View.VISIBLE);
        if(ready){
            readyIndicator.setTextColor(readyIndicator.getContext().getResources().getColor(R.color.green));
        }else{
            readyIndicator.setVisibility(View.INVISIBLE);
        }
    }

    private void setVoiceChatIndicator(boolean voiceChatOn,boolean online){
        if(voiceChatOn && online)
            voiceChatStatusImageView.setVisibility(View.VISIBLE);
        else
            voiceChatStatusImageView.setVisibility(View.INVISIBLE);
    }

    public void setUserName(String userName){
        userNameTextView.setText(userName);
    }

    public void setImage(String url) {
        if (!(url==null)) {
            Glide.with(itemView.getContext())
                    .load(url)
                    .thumbnail(0.5f)
                    .into(playerImageView);
        }else{
            playerImageView.setImageResource(R.drawable.ic_account_circle);
        }
    }
}