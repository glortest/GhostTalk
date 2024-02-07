package com.glortest.messenger.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.glortest.messenger.databinding.RecoveryCodeLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;


public class RecoveryCodeActivity extends AppCompatActivity {
    private RecoveryCodeLayoutBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RecoveryCodeLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
    }
    private void initFunc(){
        setListeners();
        setCode();
    }
    private void setListeners(){
        binding.activityRecoveryCodeButtonStartMessaging.setOnClickListener(view -> Replace.replaceActivity(this, new MainActivity(), true));
    }
    private void setCode(){
        binding.activityRecoveryCodeText.setText(preferenceManager.getString(Constants.RECOVERY_CODE));
    }
}