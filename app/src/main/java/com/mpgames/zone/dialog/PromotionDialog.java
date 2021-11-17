package com.mpgames.zone.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.mpgames.zone.R;

public class PromotionDialog {
    Dialog dialog;
    ImageView queen,rook,bishop,knight;
    public PromotionDialog(Context context, int color) {
        build(context);
        queen = dialog.findViewById(R.id.queen);
        rook = dialog.findViewById(R.id.rook);
        bishop = dialog.findViewById(R.id.bishop);
        knight = dialog.findViewById(R.id.knight);
        rook.setImageResource(getResources(1,color));
        knight.setImageResource(getResources(2,color));
        bishop.setImageResource(getResources(3,color));
        queen.setImageResource(getResources(4,color));
    }
    private int getResources(int type,int color){
        if(color==1){
            if(type==1){
                return R.drawable.rook;
            }else if(type==2){
                return R.drawable.knight;
            }else if(type==3){
                return R.drawable.bishop;
            }else if(type==4){
                return R.drawable.queen;
            }
        }else if(color==2){
            if(type==1){
                return R.drawable.rook2;
            }else if(type==2){
                return R.drawable.knight2;
            }else if(type==3){
                return R.drawable.bishop2;
            }else if(type==4){
                return R.drawable.queen2;
            }
        }
        return 0;
    }
    private void build(final Context context){
        dialog = new android.app.Dialog(context);
        dialog.setContentView(R.layout.dialog_promotion);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
    public interface ResponseListener{
        public void onPieceSelected(int type);
    }
    public void setResponseListener(final ResponseListener responseListener){
        rook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                responseListener.onPieceSelected(1);
            }
        });
        knight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                responseListener.onPieceSelected(2);
            }
        });
        bishop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                responseListener.onPieceSelected(3);
            }
        });
        queen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                responseListener.onPieceSelected(4);
            }
        });


    }
    public void show(){
        dialog.show();
    }
}
