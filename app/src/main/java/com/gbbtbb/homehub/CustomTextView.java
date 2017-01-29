package com.gbbtbb.homehub;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextView extends TextView {

    public CustomTextView(Context context) {
        super(context);

        //applyCustomFont(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //applyCustomFont(context);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //applyCustomFont(context);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        Typeface normalTypeface = Typeface.createFromAsset(getContext().getAssets(), this.getResources().getString(R.string.app_fontPath_regular));
        Typeface boldTypeface = Typeface.createFromAsset(getContext().getAssets(), this.getResources().getString(R.string.app_fontPath_bold));
        Typeface italicTypeface = Typeface.createFromAsset(getContext().getAssets(), this.getResources().getString(R.string.app_fontPath_italic));

        if (style == Typeface.BOLD) {
            super.setTypeface(boldTypeface/*, -1*/);
        }
        else if (style == Typeface.ITALIC) {
            super.setTypeface(italicTypeface/*, -1*/);
        } else {
            super.setTypeface(normalTypeface/*, -1*/);
        }
    }
/*
    private void applyCustomFont(Context context) {
        Typeface customFont = FontCache.getTypeface(this.getResources().getString(R.string.app_fontPath_regular), context);
        setTypeface(customFont);
    }
*/
}
