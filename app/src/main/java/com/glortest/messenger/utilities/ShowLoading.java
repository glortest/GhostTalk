package com.glortest.messenger.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.glortest.messenger.R;

public class ShowLoading {
    private static Dialog dialog;
    @SuppressLint("InflateParams")
    public static void show(Activity currentActivity){
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.loading);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }
    public static void dismissDialog(){
        dialog.dismiss();
    }
}
