package com.telenor.connect.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.telenor.connect.R;

public class EnableMobileDataDialogFragment extends DialogFragment {

    ContinueListener listener;

    public interface ContinueListener {
        void onContinueClicked(DialogFragment dialog);
        void onCancel(DialogInterface dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        LayoutInflater inflater = requireActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(R.string.com_telenor_connect_enable_mobile_data)
                .setMessage(R.string.com_telenor_connect_enable_mobile_data_explanation)
                .setPositiveButton(R.string.com_telenor_connect_continue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onContinueClicked(EnableMobileDataDialogFragment.this);
                    }
                });
        return builder.create();
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
