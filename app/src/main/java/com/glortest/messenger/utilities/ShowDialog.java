package com.glortest.messenger.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.glortest.messenger.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ShowDialog {
    @SuppressLint("InflateParams")
    public static void show(Activity currentActivity, String message){
        Dialog dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.dialog_widow_item);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView showDialogMessage = dialog.findViewById(R.id.showDialogMessage);
        FloatingActionButton floatingActionButton = dialog.findViewById(R.id.showDialogButtonForward);
        showDialogMessage.setText(message);
        floatingActionButton.setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.setCancelable(true);
        dialog.create();
        dialog.show();
    }
}
