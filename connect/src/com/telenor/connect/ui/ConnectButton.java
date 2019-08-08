package com.telenor.connect.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.telenor.connect.ConnectException;

public class ConnectButton extends Button {

    public ConnectButton(final Context context, final AttributeSet attributeSet) {
        super(context, attributeSet);
        AssetManager am = context.getApplicationContext().getAssets();
        setTypeface(Typeface.createFromAsset(am, "fonts/telenorregularwebfont.ttf"));
    }

    protected Activity getActivity() {
        Context context = getContext();
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (context instanceof Activity) {
            return (Activity) context;
        }
        throw new ConnectException("Unable to get Activity.");
    }
}
