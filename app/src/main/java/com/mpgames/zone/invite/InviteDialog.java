package com.mpgames.zone.invite;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.mpgames.zone.App;
import com.mpgames.zone.R;
import com.mpgames.zone.room.RoomActivity;

public class InviteDialog {
    android.app.Dialog dialog;

    public InviteDialog(final Invite invite, final Context context) {
        build(context);
        Button acceptbtn = dialog.findViewById(R.id.acceptButton);
        Button ignorebtn = dialog.findViewById(R.id.ignoreButton);
        TextView nametxt = dialog.findViewById(R.id.nameTextView);
        ImageView playerimg = dialog.findViewById(R.id.playerimg);
        nametxt.setText(invite.getSenderName());
        String photoUrl = invite.getSenderPhotoUrl();
        if(photoUrl!=null){
            Glide.with(context)
                    .load(photoUrl)
                    .into(playerimg);
        }
        acceptbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
                invite.accept();
                Intent intent = new Intent(context, RoomActivity.class);
                intent.putExtra("roomName",invite.getRoomName());
                context.startActivity(intent);

            }
        });
        ignorebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
                invite.reject();
            }
        });
    }

    public void show(){
        dialog.show();
    }
    public void hide(){
        dialog.dismiss();
    }

    private void build(final Context context){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_invite);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.TOP;
        window.setAttributes(wlp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}
