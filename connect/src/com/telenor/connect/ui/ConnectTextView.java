package com.telenor.connect.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.telenor.connect.ConnectException;

class ConnectTextView extends AppCompatTextView implements UiComponentUtils {

    public ConnectTextView(final Context context, final AttributeSet attributeSet) {
        super(context, attributeSet);
        AssetManager am = context.getApplicationContext().getAssets();
        setTypeface(Typeface.createFromAsset(am, "fonts/telenorregularwebfont.ttf"));
    }

    public Activity getActivity() {
        Context context = getContext();
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (context instanceof Activity) {
            return (Activity) context;
        }
        throw new ConnectException("Unable to get Activity while initializing the ConnectTextView component.");
    }
}
