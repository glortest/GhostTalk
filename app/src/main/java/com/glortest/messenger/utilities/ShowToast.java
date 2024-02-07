package com.glortest.messenger.utilities;

import android.content.Context;
import android.widget.Toast;

public class ShowToast {
    public static void show(Context currentContext, String message, Boolean isLong){
        if (isLong){
            Toast.makeText(currentContext, message, Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(currentContext, message, Toast.LENGTH_SHORT).show();
        }
    }
}
