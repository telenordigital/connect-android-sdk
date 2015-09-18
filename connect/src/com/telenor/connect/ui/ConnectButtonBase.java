package com.telenor.connect.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.telenor.connect.R;
import com.telenor.connect.utils.ConnectException;

public abstract class ConnectButtonBase extends Button {
    private OnClickListener internalOnClickListener;

    protected ConnectButtonBase(final Context context, final AttributeSet attributeSet) {
        super(context, attributeSet);
        setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        addOnClickListener();
    }

    public void setInternalOnClickListener(OnClickListener internalOnClickListener) {
        this.internalOnClickListener = internalOnClickListener;
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

    private void addOnClickListener() {
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectButtonBase.this.internalOnClickListener != null) {
                    ConnectButtonBase.this.internalOnClickListener.onClick(v);
                }
            }
        });
    }
}
