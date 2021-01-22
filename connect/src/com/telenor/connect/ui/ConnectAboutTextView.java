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

    private enum TelenorTypeface {
        BOLD("fonts/telenorboldwebfont.ttf"),
        MEDIUM("fonts/telenormediumwebfont.ttf"),
        REGULAR("fonts/telenorregularwebfont.ttf"),
        LIGHT("fonts/telenorlightwebfont.ttf");

        String fontPath;

        TelenorTypeface(String fontPath) {
            this.fontPath = fontPath;
        }

        public String getFontPath() {
            return fontPath;
        }
    }

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
        longDescription.append(getResources().getString(R.string.com_telenor_about_description,
                getResources().getString(ConnectSdk.getIdProvider().getNameKey())));
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

        TextView backButton = view.findViewById(R.id.connect_about_back);
        TextView slogan = view.findViewById(R.id.connect_about_slogan);
        TextView title = view.findViewById(R.id.connect_about_title);
        TextView paragraph1 = view.findViewById(R.id.connect_about_p1);
        TextView paragraph2 = view.findViewById(R.id.connect_about_p2);
        TextView paragraph3 = view.findViewById(R.id.connect_about_p3);
        TextView paragraph4 = view.findViewById(R.id.connect_about_p4);
        TextView paragraph5 = view.findViewById(R.id.connect_about_p5);
        TextView paragraph6 = view.findViewById(R.id.connect_about_p6);
        ImageView logo = view.findViewById(R.id.connect_about_logo);

        backButton.setTypeface(Typeface.createFromAsset(am, TelenorTypeface.REGULAR.getFontPath()));
        slogan.setTypeface(Typeface.createFromAsset(am, TelenorTypeface.LIGHT.getFontPath()));
        title.setTypeface(Typeface.createFromAsset(am, TelenorTypeface.MEDIUM.getFontPath()));
        paragraph1.setTypeface(Typeface.createFromAsset(am, TelenorTypeface.LIGHT.getFontPath()));
        paragraph2.setTypeface(Typeface.createFromAsset(am, TelenorTypeface.LIGHT.getFontPath()));
        paragraph3.setTypeface(Typeface.createFromAsset(am, TelenorTypeface.LIGHT.getFontPath()));
        paragraph4.setTypeface(Typeface.createFromAsset(am, TelenorTypeface.LIGHT.getFontPath()));
        paragraph5.setTypeface(Typeface.createFromAsset(am, TelenorTypeface.LIGHT.getFontPath()));
        paragraph6.setTypeface(Typeface.createFromAsset(am, TelenorTypeface.LIGHT.getFontPath()));

        title.setText(getResources().getString(R.string.com_telenor_about_screen_title,
                getResources().getString(ConnectSdk.getIdProvider().getNameKey())));
        paragraph1.setText(getResources().getString(R.string.com_telenor_about_p1,
                getResources().getString(ConnectSdk.getIdProvider().getNameKey())));
        paragraph2.setText(getResources().getString(R.string.com_telenor_about_p2,
                getResources().getString(ConnectSdk.getIdProvider().getSubscribersKey()),
                getResources().getString(ConnectSdk.getIdProvider().getNetworkKey())));
        paragraph3.setText(getResources().getString(R.string.com_telenor_about_p3,
                getResources().getString(ConnectSdk.getIdProvider().getNameKey())));
        paragraph4.setText(getResources().getString(R.string.com_telenor_about_p4,
                getResources().getString(ConnectSdk.getIdProvider().getNameKey())));
        paragraph5.setText(getResources().getString(R.string.com_telenor_about_p5,
                getResources().getString(ConnectSdk.getIdProvider().getNameKey()),
                getResources().getString(ConnectSdk.getIdProvider().getSubscribersKey())));
        paragraph6.setText(getResources().getString(R.string.com_telenor_about_p6,
                getResources().getString(ConnectSdk.getIdProvider().getNameKey())));
        logo.setImageResource(ConnectSdk.getIdProvider().getLogoKey());

        if (popupWindow != null) {
            popupWindow.dismiss();
        }

        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setAnimationStyle(R.style.com_telenor_popup_animation);
        popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0);

        backButton.setOnClickListener(new OnClickListener() {
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
