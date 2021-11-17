package com.mpgames.zone.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.mpgames.zone.R;

public class ConfirmDialog {
    android.app.Dialog dialog;

    public ConfirmDialog(Context context, String title, String message, final Runnable onPositiveInput, final Runnable onNegativaInput) {
        build(context);
        TextView titletxt = dialog.findViewById(R.id.titletxt);
        titletxt.setText(title);
        TextView messagetxt = dialog.findViewById(R.id.messagetxt);
        messagetxt.setText(message);
        Button yesbtn = dialog.findViewById(R.id.yesbtn);
        yesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                onPositiveInput.run();
            }
        });
        Button nobtn = dialog.findViewById(R.id.nobtn);
        nobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if(onNegativaInput!=null){
                    onNegativaInput.run();
                }
            }
        });
    }

    private void build(final Context context){
        dialog = new android.app.Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void show(){
        dialog.show();
    }

    public void hide(){
        dialog.hide();
    }
}
