package com.mpgames.zone.userlist;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.mpgames.zone.R;

public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView, userNameTextView,descriptionTextView;
        private final ImageView playerImageView,statusImageView;
        private final Button inviteButton;
        private final LinearLayout layout;
        final LinearLayout.LayoutParams params;

        public ViewHolder(final View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            userNameTextView = itemView.findViewById(R.id.usernametxt);
            descriptionTextView = itemView.findViewById(R.id.desctxt);
            playerImageView = itemView.findViewById(R.id.playerimg);
            statusImageView = itemView.findViewById(R.id.statusimg);
            inviteButton = itemView.findViewById(R.id.invitebtn);
            layout = itemView.findViewById(R.id.chat_layout);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public void setName(String name) {
            nameTextView.setText(name);
        }
        public void hide() {
            params.height = 0;
            layout.setLayoutParams(params);

        }

        public  Button getInviteButton(){
            return inviteButton;
        }

        public void setStatusIndicator(boolean online,boolean inRoom){
            if(online){
                inviteButton.setVisibility(View.VISIBLE);
                statusImageView.setVisibility(View.VISIBLE);
                if(inRoom){
                    inviteButton.setEnabled(false);
                    statusImageView.setImageResource(R.drawable.yellow_dot);
                }else{
                    inviteButton.setEnabled(true);
                    statusImageView.setImageResource(R.drawable.green_dot);
                }
            }else{
                inviteButton.setVisibility(View.INVISIBLE);
                statusImageView.setVisibility(View.INVISIBLE);
            }
        }

        public void setUserName(String userName){
            userNameTextView.setText(userName);
        }
        public void setDescription(String description){
            descriptionTextView.setText(description);
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