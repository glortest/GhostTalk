package com.glortest.messenger.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

import com.glortest.messenger.R;
import com.glortest.messenger.databinding.ChangeArchivePasswordLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.glortest.messenger.utilities.ShowToast;


public class ChangeArchivePasswordActivity extends BaseActivity {
    private ChangeArchivePasswordLayoutBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChangeArchivePasswordLayoutBinding.inflate(getLayoutInflater());
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
                changePassword();
            } else if (binding.activityChangePasswordNew.getText().toString().trim().length() < Constants.MINIMUM_PASSWORD_LENGTH){
                ShowDialog.show(this, getResources().getString(R.string.password_is_too_short));
            } else {
                changePassword();
            }
        });
    }
    private void changePassword(){
        if(binding.activityChangePasswordNew.getText().toString().trim().isEmpty()){
            ShowLoading.show(this);
            preferenceManager.putString(Constants.ARCHIVE_PASSWORD, null);
            ShowToast.show(this, getResources().getString(R.string.archive_password_updated_successfully), true);
            onBackPressed();
        }
        else if (!binding.activityChangePasswordNew.getText().toString().trim().equals(preferenceManager.getString(Constants.ARCHIVE_PASSWORD))){
            ShowLoading.show(this);
            preferenceManager.putString(Constants.ARCHIVE_PASSWORD, binding.activityChangePasswordNew.getText().toString().trim());
            ShowToast.show(this, getResources().getString(R.string.archive_password_updated_successfully), true);
            onBackPressed();
        }
    }
    private void setUserInfo(){
        if(preferenceManager.getString(Constants.ARCHIVE_PASSWORD) != null)
            binding.activityChangePasswordNew.setText(preferenceManager.getString(Constants.ARCHIVE_PASSWORD));
    }
    private void setMaxLength(){
        binding.activityChangePasswordNew.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.PASSWORD_MAX_LENGTH)});
    }
    @SuppressLint("SetTextI18n")
    private void setCount(){
        binding.activityChangePasswordCount.setText(binding.activityChangePasswordNew.getText().toString().trim().length() + getResources().getString(R.string.delimiter) + Constants.PASSWORD_MAX_LENGTH);
    }
}