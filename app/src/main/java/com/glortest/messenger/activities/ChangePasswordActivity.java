package com.glortest.messenger.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

import com.glortest.messenger.R;
import com.glortest.messenger.databinding.ChangePasswordLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.glortest.messenger.utilities.ShowToast;


public class ChangePasswordActivity extends BaseActivity {
    private ChangePasswordLayoutBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChangePasswordLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
    }
    private void initFunc(){
        setListeners();
        setUserInfo();
        binding.activityChangePasswordNew.requestFocus();
        setMaxLength();
        setCount();
    }
    private void setListeners(){
        binding.activityChangePasswordNew.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setCount();
            }
        });
        binding.activityChangePasswordButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityChangePasswordButtonCheck.setOnClickListener(view -> {
            if (binding.activityChangePasswordNew.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.password_can_not_be_empty));
            } else if (binding.activityChangePasswordNew.getText().toString().trim().length() < Constants.MINIMUM_PASSWORD_LENGTH){
                ShowDialog.show(this, getResources().getString(R.string.password_is_too_short));
            } else {
                changePassword();
            }
        });
    }
    private void changePassword(){
        if (!binding.activityChangePasswordNew.getText().toString().trim().equals(preferenceManager.getString(Constants.PASSWORD))){
            ShowLoading.show(this);
            InitFirebase.firebaseFirestore.collection(Constants.USERS)
                    .document(preferenceManager.getString(Constants.USER_ID)).update(Constants.PASSWORD, binding.activityChangePasswordNew.getText().toString().trim())
                    .addOnSuccessListener(unused -> {
                        ShowLoading.dismissDialog();
                        preferenceManager.putString(Constants.PASSWORD, binding.activityChangePasswordNew.getText().toString().trim());
                        ShowToast.show(this, getResources().getString(R.string.password_updated_successfully), false);
                        onBackPressed();
                    }).addOnFailureListener(e -> {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(this, getResources().getString(R.string.error));
                    });
        }
    }
    private void setUserInfo(){
        binding.activityChangePasswordNew.setText(preferenceManager.getString(Constants.PASSWORD));
    }
    private void setMaxLength(){
        binding.activityChangePasswordNew.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.PASSWORD_MAX_LENGTH)});
    }
    @SuppressLint("SetTextI18n")
    private void setCount(){
        binding.activityChangePasswordCount.setText(binding.activityChangePasswordNew.getText().toString().trim().length() + getResources().getString(R.string.delimiter) + Constants.PASSWORD_MAX_LENGTH);
    }
}