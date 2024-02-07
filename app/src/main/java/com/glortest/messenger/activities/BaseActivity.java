package com.glortest.messenger.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;

import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;

public class BaseActivity extends AppCompatActivity {
    private DocumentReference documentReference;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        initFunc();
    }
    private void initFields(){
        InitFirebase.init();
        preferenceManager = new PreferenceManager(this);
        documentReference = InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .document(preferenceManager.getString(Constants.USER_ID));
    }
    private void initFunc(){}

    @Override
    protected void onPause() {
        super.onPause();
        if (preferenceManager.getBoolean(Constants.STATUS)) {
            documentReference.update(Constants.AVAILABLE, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferenceManager.getBoolean(Constants.STATUS)) {
            documentReference.update(Constants.AVAILABLE, 1);
        }
    }
}
