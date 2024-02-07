package com.glortest.messenger.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.glortest.messenger.R;
import com.glortest.messenger.databinding.CheckPasswordLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;

import java.util.Random;


public class CheckPasswordActivity extends BaseActivity {
    private CheckPasswordLayoutBinding binding;
    private PreferenceManager preferenceManager;
    private Boolean type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CheckPasswordLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
        type = getIntent().getBooleanExtra(Constants.ARGUMENT_PASSWORD, false);
    }
    private void initFunc(){
        setListeners();
        binding.activityCheckPasswordNew.requestFocus();
    }
    private void setListeners(){
        binding.activityCheckPasswordButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityCheckPasswordButtonCheck.setOnClickListener(view -> {
            if (binding.activityCheckPasswordNew.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.password_can_not_be_empty));
            }else if (binding.activityCheckPasswordNew.getText().toString().trim().length() < Constants.MINIMUM_PASSWORD_LENGTH){
                ShowDialog.show(this, getResources().getString(R.string.password_is_too_short));
            } else {
                checkPassword();
            }
        });
    }
    @SuppressLint("DefaultLocale")
    public static String getRecoveryCode() {
        Random random = new Random();
        int number = random.nextInt(99999999);

        return String.format("%08d", number);
    }
    private void checkPassword(){
        if (binding.activityCheckPasswordNew.getText().toString().trim().equals(preferenceManager.getString(Constants.PASSWORD))){
            if (type){
                Replace.replaceActivity(this, new ChangePasswordActivity(), true);
            } else {
                String recoveryCode = getRecoveryCode();
                ShowLoading.show(this);
                InitFirebase.firebaseFirestore.collection(Constants.USERS)
                        .document(preferenceManager.getString(Constants.USER_ID)).update(Constants.RECOVERY_CODE, recoveryCode)
                        .addOnSuccessListener(unused -> {
                            ShowLoading.dismissDialog();
                            preferenceManager.putString(Constants.RECOVERY_CODE, recoveryCode);
                            Replace.replaceActivity(this, new RecoveryCodeActivity(), true);
                        }).addOnFailureListener(e -> {
                            ShowLoading.dismissDialog();
                            ShowDialog.show(this, getResources().getString(R.string.error));
                        });
            }
        } else {
            ShowDialog.show(this, getResources().getString(R.string.incorrect_password));
        }
    }
}