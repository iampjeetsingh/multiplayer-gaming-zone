package com.mpgames.zone.teenpatti;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mpgames.zone.R;

public class CardLayout extends ConstraintLayout {
    private TextView textView1,textView2;
    private ImageView imageView;

    public CardLayout(Context context) {
        super(context);
        create();
    }

    public CardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        create();
    }

    public CardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create();
    }

    private void create(){
        int id1=101,id2=102,id3=103;
        textView1 = new TextView(getContext());
        textView1.setId(id1);
        textView2 = new TextView(getContext());
        textView2.setId(id2);
        imageView = new ImageView(getContext());
        imageView.setId(id3);
        textView1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        textView2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        ConstraintLayout.LayoutParams textView1Params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        ConstraintLayout.LayoutParams textView2Params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        ConstraintLayout.LayoutParams imageViewParams = new ConstraintLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        textView1.setVisibility(INVISIBLE);
        textView2.setVisibility(INVISIBLE);
        imageView.setVisibility(INVISIBLE);
        addView(imageView,imageViewParams);
        addView(textView1,textView1Params);
        addView(textView2,textView2Params);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);

        constraintSet.connect(id1, ConstraintSet.TOP, getId(), ConstraintSet.TOP, 0);
        constraintSet.connect(id1, ConstraintSet.START, getId(), ConstraintSet.START, dp(5));

        constraintSet.connect(id2, ConstraintSet.BOTTOM, getId(), ConstraintSet.BOTTOM, 0);
        constraintSet.connect(id2, ConstraintSet.END, getId(), ConstraintSet.END, dp(5));

        constraintSet.connect(id3, ConstraintSet.TOP, getId(), ConstraintSet.TOP, 0);
        constraintSet.connect(id3, ConstraintSet.BOTTOM, getId(), ConstraintSet.BOTTOM, 0);
        constraintSet.connect(id3, ConstraintSet.START, getId(), ConstraintSet.START, dp(10));
        constraintSet.connect(id3, ConstraintSet.END, getId(), ConstraintSet.END, dp(10));

        constraintSet.applyTo(this);
    }

    private int dp(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void free(){
        textView1 = null;
        textView2 = null;
        imageView = null;
        removeAllViewsInLayout();
    }

    public void show(){
        setBackgroundColor(getResources().getColor(R.color.white));
        textView1.setVisibility(VISIBLE);
        textView2.setVisibility(VISIBLE);
        imageView.setVisibility(VISIBLE);
    }

    public void setCard(Card card){
        String text = "";
        int number = card.getNumber(),type=card.getType();
        if(1<number && number<11)
            text = ""+number;
        else if(number==1)
            text = "A";
        else if(number==11)
            text = "J";
        else if(number==12)
            text = "Q";
        else if(number==13)
            text = "K";
        textView1.setTextColor(getResources().getColor(R.color.black));
        textView2.setTextColor(getResources().getColor(R.color.black));
        Drawable drawable = null;
        if(type==Card.SPADE) {
            drawable = getResources().getDrawable(R.drawable.spade);
        }else if(type==Card.CLUB) {
            drawable = getResources().getDrawable(R.drawable.club);
        }else if(type==Card.HEART) {
            drawable = getResources().getDrawable(R.drawable.heart);
            textView1.setTextColor(getResources().getColor(R.color.red));
            textView2.setTextColor(getResources().getColor(R.color.red));
        }else if(type==Card.DIAMOND){
            drawable = getResources().getDrawable(R.drawable.diamond);
            textView1.setTextColor(getResources().getColor(R.color.red));
            textView2.setTextColor(getResources().getColor(R.color.red));
        }
        textView1.setText(text);
        textView2.setText(text);
        Glide.with(getContext()).load(drawable).apply(RequestOptions.sizeMultiplierOf(0.5f)).into(imageView);
    }
}
