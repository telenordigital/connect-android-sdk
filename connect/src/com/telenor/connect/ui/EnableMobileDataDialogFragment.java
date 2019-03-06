package com.telenor.connect.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.telenor.connect.R;
import com.telenor.connect.headerenrichment.HeLogic;

public class EnableMobileDataDialogFragment extends DialogFragment {

    private ContinueListener listener;
    private View.OnClickListener continueCallback = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            listener.onContinueClicked(EnableMobileDataDialogFragment.this);
        }
    };

    public interface ContinueListener {
        void onContinueClicked(DialogFragment dialog);
        void onCancel(DialogInterface dialog);
    }

    @NonNull
    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.com_telenor_connect_fragment_enable_mobile_data, null);
        final View automaticSignInButton = view
                .findViewById(R.id.com_telenor_connect_fragment_enable_mobile_data_sign_in_automatically_button);
        automaticSignInButton.setOnClickListener(continueCallback);
        view.findViewById(R.id.com_telenor_connect_fragment_enable_mobile_data_sign_in_regular_button)
                .setOnClickListener(continueCallback);

        HeLogic.registerCellularNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                automaticSignInButton.setEnabled(network != null);
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    public void setContinueListener(ContinueListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStop() {
        super.onStop();
        listener.onCancel(null);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        listener.onCancel(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        listener.onCancel(dialog);
    }
}
