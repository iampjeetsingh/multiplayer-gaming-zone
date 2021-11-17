package com.mpgames.zone.teenpatti;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mpgames.zone.R;
import com.mpgames.zone.room.Player;

public class PlayerLayout extends LinearLayout {
    private TextView nameTextView,actionTextView;
    private ImageView imageView;

    public PlayerLayout(Context context) {
        super(context);
        create();
    }

    public PlayerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        create();
    }

    public PlayerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create();
    }

    private void create(){
        int id1=101,id2=102,id3=103;
        nameTextView = new TextView(getContext());
        nameTextView.setId(id1);
        actionTextView = new TextView(getContext());
        actionTextView.setId(id2);
        imageView = new ImageView(getContext());
        imageView.setId(id3);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        nameTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        actionTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        nameTextView.setTextColor(getResources().getColor(R.color.black));
        actionTextView.setTextColor(getResources().getColor(R.color.black));
        nameTextView.setMaxLines(1);
        actionTextView.setMaxLines(2);
        LayoutParams imageViewParams = new LayoutParams(dp(60),dp(60));
        imageView.setLayoutParams(imageViewParams);
        LayoutParams textViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameTextView.setLayoutParams(textViewParams);
        actionTextView.setLayoutParams(textViewParams);
        nameTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        actionTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        Glide.with(getContext())
                .load(getResources().getDrawable(R.drawable.ic_account_circle))
                .apply(RequestOptions.sizeMultiplierOf(0.5f))
                .into(imageView);
        nameTextView.setText("Anonymous");
        actionTextView.setText("Boot"+"\n"+"Rs.70");
        addView(nameTextView);
        addView(imageView);
        addView(actionTextView);
    }

    private int dp(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void free(){
        nameTextView = null;
        actionTextView = null;
        imageView = null;
        removeAllViewsInLayout();
    }

    public void setLastMove(Move move){
        if(move!=null){
            String text="";
            int action = move.getAction();
            if(action==Move.CHAAL || action==Move.RAISE){
                text = "Chaal";
            }else if(action==Move.PACK || action==Move.SHOW){
                text = "Pack";
            }else if(action==Move.BLIND){
                text = "Blind";
            }
            actionTextView.setText(text+"\nRs."+move.getAmount());
        }else{
            actionTextView.setText("Boot"+"\n"+"Rs.70");
        }
    }

    public void setPlayer(Player player){
        if(player==null){
            hide();
        }else{
            show();
            if(player.getPhotoUrl()!=null){
                Glide.with(getContext())
                        .load(player.getPhotoUrl())
                        .apply(RequestOptions.sizeMultiplierOf(0.5f))
                        .into(imageView);
            }
            if(player.getName()!=null)
                nameTextView.setText(player.getName());
            else
                nameTextView.setText("Anonymous");
        }
    }

    private void show(){
        setVisibility(VISIBLE);
    }

    public void hide(){
        imageView.setImageDrawable(null);
        nameTextView.setText("");
        actionTextView.setText("");
        setVisibility(INVISIBLE);
    }
}
