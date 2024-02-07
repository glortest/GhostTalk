package com.glortest.messenger.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.glortest.messenger.R;
import com.glortest.messenger.databinding.ArchivePasswordLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.glortest.messenger.utilities.ShowToast;

public class ArchivePasswordActivity extends BaseActivity {
    private ArchivePasswordLayoutBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ArchivePasswordLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
    }
    private void initFunc(){
        setListeners();
        binding.activityChangePasswordNew.requestFocus();
    }
    private void setListeners(){
        binding.activityChangePasswordButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityChangePasswordButtonCheck.setOnClickListener(view -> {
            if (binding.activityChangePasswordNew.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.password_can_not_be_empty));
            } else if (binding.activityChangePasswordNew.getText().toString().trim().length() < Constants.MINIMUM_PASSWORD_LENGTH){
                ShowDialog.show(this, getResources().getString(R.string.password_is_too_short));
            } else if(binding.activityChangePasswordNew.getText().toString().equals(preferenceManager.getString(Constants.ARCHIVE_PASSWORD))){
                View view_focus = this.getCurrentFocus();
                if (view_focus != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view_focus.getWindowToken(), 0);
                }
                Replace.replaceActivity(this, new ArchiveActivity(), false);
            }
        });
    }
    private void changePassword(){
        if (!binding.activityChangePasswordNew.getText().toString().trim().equals(preferenceManager.getString(Constants.PASSWORD))){
            ShowLoading.show(this);
            preferenceManager.putString(Constants.ARCHIVE_PASSWORD, binding.activityChangePasswordNew.getText().toString().trim());
            ShowToast.show(this, getResources().getString(R.string.archive_password_updated_successfully), true);
            onBackPressed();
        }
    }
}