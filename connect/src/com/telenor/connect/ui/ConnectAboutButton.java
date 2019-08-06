package com.telenor.connect.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.telenor.connect.R;

public class ConnectAboutButton extends ConnectButton {

    PopupWindow popupWindow;

    public ConnectAboutButton(final Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        AssetManager am = context.getApplicationContext().getAssets();
        setTypeface(Typeface.createFromAsset(am, "fonts/telenorregularwebfont.ttf"));
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openPopup(context);
            }
        });
    }

    public void openPopup(Context context) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.com_telenor_connect_about, null, false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            view.setPadding(0, spToPx(context, 20.0f), 0, 0);
        }

        AssetManager am = context.getApplicationContext().getAssets();

        TextView title = view.findViewById(R.id.connect_about_title);
        TextView subTitle = view.findViewById(R.id.connect_about_subtitle);
        TextView description = view.findViewById(R.id.connect_about_description);
        ImageView closeIcon = view.findViewById(R.id.connect_about_close);

        title.setTypeface(Typeface.createFromAsset(am, "fonts/telenorboldwebfont.ttf"));
        subTitle.setTypeface(Typeface.createFromAsset(am, "fonts/telenorregularwebfont.ttf"));
        description.setTypeface(Typeface.createFromAsset(am, "fonts/telenorlightwebfont.ttf"));

        if (popupWindow != null) {
            popupWindow.dismiss();
        }

        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setAnimationStyle(R.style.com_telenor_popup_animation);
        popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0);

        closeIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private int spToPx(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

}
