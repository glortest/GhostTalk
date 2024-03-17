package com.glortest.messenger.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import com.glortest.messenger.R;
import com.glortest.messenger.databinding.ChangeUsernameLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.glortest.messenger.utilities.ShowToast;


public class ChangeUsernameActivity extends AppCompatActivity {
    private ChangeUsernameLayoutBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChangeUsernameLayoutBinding.inflate(getLayoutInflater());
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
        binding.activityChangeUsernameNew.requestFocus();
        setMaxLength();
        setCount();
    }
    @SuppressLint("SetTextI18n")
    private void setListeners(){
        binding.activityChangeUsernameNew.addTextChangedListener(new TextWatcher() {
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
        binding.activityChangeUsernameButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityChangeUsernameButtonCheck.setOnClickListener(view -> {
            if (!binding.activityChangeUsernameNew.getText().toString().trim().contains(Constants.USERNAME_SIGN) && !binding.activityChangeUsernameNew.getText().toString().trim().isEmpty()){
                binding.activityChangeUsernameNew.setText(Constants.USERNAME_SIGN + binding.activityChangeUsernameNew.getText().toString().trim());
            }
            if (binding.activityChangeUsernameNew.getText().toString().trim().length() < 2){
                ShowDialog.show(this, getResources().getString(R.string.nickname_can_not_be_empty));
            }else if (!binding.activityChangeUsernameNew.getText().toString().trim().equals(binding.activityChangeUsernameNew.getText().toString().trim().toLowerCase())){
                ShowDialog.show(this, getResources().getString(R.string.nickname_must_be_in_lower_case));
            }else if ((binding.activityChangeUsernameNew.getText().toString().trim().contains(" "))){
                ShowDialog.show(this, getResources().getString(R.string.nickname_must_be_without_spaces));
            }else {
                changeUsername();
            }
        });
    }
    private void changeUsername(){
        if (!binding.activityChangeUsernameNew.getText().toString().trim().equals(preferenceManager.getString(Constants.USERNAME))){
            ShowLoading.show(this);
            InitFirebase.firebaseFirestore.collection(Constants.USERS)
                    .whereEqualTo(Constants.USERNAME, binding.activityChangeUsernameNew.getText().toString().trim())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                            ShowLoading.dismissDialog();
                            ShowDialog.show(this, getResources().getString(R.string.this_nickname_is_already_linked_to_the_account));
                        } else {
                            InitFirebase.firebaseFirestore.collection(Constants.USERS)
                                    .document(preferenceManager.getString(Constants.USER_ID))
                                    .update(Constants.USERNAME, binding.activityChangeUsernameNew.getText().toString().trim())
                                    .addOnSuccessListener(unused -> {
                                        ShowLoading.dismissDialog();
                                        preferenceManager.putString(Constants.USERNAME, binding.activityChangeUsernameNew.getText().toString().trim());
                                        ShowToast.show(this, getResources().getString(R.string.nickname_updated_successfully), false);
                                        onBackPressed();
                                    }).addOnFailureListener(e -> {
                                        ShowLoading.dismissDialog();
                                        ShowDialog.show(this, getResources().getString(R.string.error));
                                    });
                        }
                    });
        }
    }
    private void setUserInfo(){
        binding.activityChangeUsernameNew.setText(preferenceManager.getString(Constants.USERNAME));
    }
    private void setMaxLength(){
        binding.activityChangeUsernameNew.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.USERNAME_MAX_LENGTH)});
    }
    @SuppressLint("SetTextI18n")
    private void setCount(){
        binding.activityChangeUsernameCount.setText(binding.activityChangeUsernameNew.getText().toString().trim().length() + getResources().getString(R.string.delimiter) + Constants.USERNAME_MAX_LENGTH);
    }
}