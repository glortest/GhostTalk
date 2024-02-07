package com.glortest.messenger.utilities;

import android.app.Activity;
import android.content.Intent;

public class Replace {
    public static void replaceActivity(Activity currentActivity, Activity toActivity, Boolean isFinish){
        if (isFinish){
            Intent intent = new Intent(currentActivity, toActivity.getClass());
            currentActivity.startActivity(intent);
            currentActivity.finish();
        }else {
            Intent intent = new Intent(currentActivity, toActivity.getClass());
            currentActivity.startActivity(intent);
        }
    }
}
