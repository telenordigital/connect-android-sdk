package com.telenor.connect.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;

public class ConnectAboutTextView extends ConnectTextView {

    private PopupWindow popupWindow;

    public ConnectAboutTextView(final Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initControls();
        initVisuals(context);
    }

    private void initControls() {
        setAllCaps(false);
        setFocusable(true);
        setClickable(true);
    }

    private void initVisuals(final Context context) {
        final AssetManager am = context.getApplicationContext().getAssets();
        setTypeface(Typeface.createFromAsset(am, "fonts/telenorlightwebfont.ttf"));
        CharSequence linkText = getResources().getText(R.string.com_telenor_about_link);
        SpannableString spannableLinkText = new SpannableString(linkText);
        ClickableSpan clickableSpannableLinkText = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View textView) {
                openPopup(context);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setTypeface(Typeface.createFromAsset(am, "fonts/telenorboldwebfont.ttf"));
                ds.setColor(getCurrentTextColor());
                ds.setUnderlineText(false);
            }
        };
        SpannableStringBuilder longDescription = new SpannableStringBuilder();
        longDescription.append(getResources().getString(R.string.com_telenor_about_description, ConnectSdk.getIdProvider().getName()));
        spannableLinkText.setSpan(clickableSpannableLinkText, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        longDescription.append(spannableLinkText);
        setText(longDescription, TextView.BufferType.SPANNABLE);
        setMovementMethod(LinkMovementMethod.getInstance());
        setHighlightColor(Color.TRANSPARENT);
    }

    private void openPopup(Context context) {
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
        title.setText(getResources().getString(R.string.com_telenor_about_screen_title, ConnectSdk.getIdProvider().getName()));
        subTitle.setTypeface(Typeface.createFromAsset(am, "fonts/telenorregularwebfont.ttf"));
        subTitle.setText(getResources().getString(R.string.com_telenor_about_screen_subtitle, ConnectSdk.getIdProvider().getName()));
        description.setTypeface(Typeface.createFromAsset(am, "fonts/telenorlightwebfont.ttf"));
        description.setText(getResources().getString(R.string.com_telenor_about_screen_description, ConnectSdk.getIdProvider().getName()));

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
