package com.task.mobi.customViews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class MyCheckBox extends android.support.v7.widget.AppCompatCheckBox {
    Context context;
    int defStyleAttr;
    private AttributeSet attrs;

    public MyCheckBox(Context context) {
        super(context);
        Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        this.setTypeface(face);
        this.context = context;
        init();
    }


    public MyCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        init();
    }

    public MyCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        this.defStyleAttr = defStyleAttr;
        init();
    }


    @Override
    public void setTypeface(Typeface tf) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
        super.setTypeface(tf);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
        super.setTypeface(tf, style);
    }

    private void init() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
        this.setTypeface(font);
    }
}
