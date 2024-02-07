package com.glortest.messenger.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.glortest.messenger.R;
import com.glortest.messenger.activities.MainActivity;
import com.glortest.messenger.databinding.NewPasswordLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;

import org.jetbrains.annotations.NotNull;


public class NewPasswordFragment extends Fragment {
    private NewPasswordLayoutBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = NewPasswordLayoutBinding.inflate(getLayoutInflater());
        initFields();
        initFunc();
        return binding.getRoot();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(requireActivity());
    }
    private void initFunc(){
        setListeners();
        setMaxLength();
        setCount();
    }
    private void setListeners(){
        binding.activityNewPasswordNew.addTextChangedListener(new TextWatcher() {
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
        binding.activityNewPasswordButtonBack.setOnClickListener(view -> requireActivity().onBackPressed());
        binding.activityNewPasswordButtonCheck.setOnClickListener(view -> {
            if (binding.activityNewPasswordNew.getText().toString().trim().isEmpty()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.password_can_not_be_empty));
            } else if (binding.activityNewPasswordNew.getText().toString().trim().length() < Constants.MINIMUM_PASSWORD_LENGTH){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.password_is_too_short));
            } else {
                setNewPassword();
            }
        });
    }
    private void setNewPassword(){
        if (binding.activityNewPasswordNew.getText().toString().trim().equals(preferenceManager.getString(Constants.PASSWORD))){
            ShowDialog.show(requireActivity(), getResources().getString(R.string.you_cannot_use_your_password_again_choose_a_different_one));
        } else {
            ShowLoading.show(requireActivity());
            InitFirebase.firebaseFirestore.collection(Constants.USERS)
                    .document(preferenceManager.getString(Constants.USER_ID))
                    .update(Constants.PASSWORD, binding.activityNewPasswordNew.getText().toString().trim())
                    .addOnSuccessListener(unused -> {
                        ShowLoading.dismissDialog();
                        preferenceManager.putBoolean(Constants.IS_SIGNED_IN, true);
                        preferenceManager.putBoolean(Constants.STATUS, true);
                        preferenceManager.putString(Constants.PASSWORD, binding.activityNewPasswordNew.getText().toString().trim());
                        Replace.replaceActivity(requireActivity(), new MainActivity(), true);
                    })
                    .addOnFailureListener(e -> {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(requireActivity(), getResources().getString(R.string.error));
                    });
        }
    }
    private void setMaxLength(){
        binding.activityNewPasswordNew.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.PASSWORD_MAX_LENGTH)});
    }
    @SuppressLint("SetTextI18n")
    private void setCount(){
        binding.activityNewPasswordCount.setText(binding.activityNewPasswordNew.getText().toString().trim().length() + getResources().getString(R.string.delimiter) + Constants.PASSWORD_MAX_LENGTH);
    }
}