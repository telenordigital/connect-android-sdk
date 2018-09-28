package com.telenor.connect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.telenor.connect.R;

public class WebErrorView extends RelativeLayout {
    private TextView errorText;
    private View loadingSpinner;
    private Button tryAgain;
    private Button showMoreToggle;
    private TextView errorDetails;

    public WebErrorView(Context context) {
        super(context);
        init();
    }

    public WebErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WebErrorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebErrorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.com_telenor_connect_error_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        errorText = findViewById(R.id.com_telenor_connect_error_view_text);
        loadingSpinner = findViewById(R.id.com_telenor_connect_error_view_loading);
        tryAgain = findViewById(R.id.com_telenor_connect_error_view_try_again);
        findViewById(R.id.com_telenor_connect_error_view_network_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                getContext().startActivity(intent);
            }
        });
        errorDetails = findViewById(R.id.com_telenor_connect_error_view_details);
        showMoreToggle = findViewById(R.id.com_telenor_connect_error_view_show_more_toggle);
        showMoreToggle.setPaintFlags(showMoreToggle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        showMoreToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int newVisibility = errorDetails.getVisibility() != VISIBLE ? VISIBLE : GONE;
                errorDetails.setVisibility(errorDetails.getVisibility() != VISIBLE ? VISIBLE : GONE);
                showMoreToggle.setText(newVisibility != VISIBLE ? R.string.com_telenor_connect_show_details : R.string.com_telenor_connect_hide_details);
            }
        });
    }

    public void setErrorText(String text, String details) {
        errorText.setText(text != null ? text : "");
        errorDetails.setText(details != null ? details : "");
    }

    public View getLoadingSpinner() {
        return loadingSpinner;
    }

    public Button getTryAgainButton() {
        return tryAgain;
    }
}
