package com.glortest.messenger.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

import com.glortest.messenger.R;
import com.glortest.messenger.databinding.ChangeBioLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.glortest.messenger.utilities.ShowToast;


public class ChangeBioActivity extends BaseActivity {
    private ChangeBioLayoutBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChangeBioLayoutBinding.inflate(getLayoutInflater());
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
        binding.activityChangeBioNew.requestFocus();
        setMaxLength();
        setCount();
    }
    private void setUserInfo(){
        binding.activityChangeBioNew.setText(preferenceManager.getString(Constants.BIO));
    }
    private void setListeners(){
        binding.activityChangeBioButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityChangeBioNew.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                setCount();
            }
        });
        binding.activityChangeBioButtonCheck.setOnClickListener(view -> {
            if (binding.activityChangeBioNew.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.bio_can_not_be_empty));
            } else {
                changeBio();
            }
        });
    }
    private void changeBio(){
        if (!binding.activityChangeBioNew.getText().toString().trim().equals(preferenceManager.getString(Constants.BIO))){
            ShowLoading.show(this);
            InitFirebase.firebaseFirestore.collection(Constants.USERS)
                    .document(preferenceManager.getString(Constants.USER_ID))
                    .update(Constants.BIO, binding.activityChangeBioNew.getText().toString().trim())
                    .addOnSuccessListener(unused -> {
                        ShowLoading.dismissDialog();
                        preferenceManager.putString(Constants.BIO, binding.activityChangeBioNew.getText().toString().trim());
                        ShowToast.show(this, getResources().getString(R.string.bio_updated_successfully), false);
                        onBackPressed();
                    }).addOnFailureListener(e -> {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(this, getResources().getString(R.string.error));
                    });
        }
    }
    private void setMaxLength(){
        binding.activityChangeBioNew.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.BIO_MAX_LENGTH)});
    }
    @SuppressLint("SetTextI18n")
    private void setCount(){
        binding.activityChangeBioCount.setText(binding.activityChangeBioNew.getText().toString().trim().length() + getResources().getString(R.string.delimiter) + Constants.BIO_MAX_LENGTH);
    }
}