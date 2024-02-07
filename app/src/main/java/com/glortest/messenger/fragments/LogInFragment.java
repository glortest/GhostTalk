package com.glortest.messenger.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.glortest.messenger.R;
import com.glortest.messenger.activities.RecoveryCodeActivity;
import com.glortest.messenger.databinding.LogInLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;


public class LogInFragment extends Fragment {
    private LogInLayoutBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = LogInLayoutBinding.inflate(getLayoutInflater());
        initFields();
        initFunc();
        return binding.getRoot();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(requireActivity());
    }
    private void initFunc(){
        setListeners();
        preferenceManager.clear();
        setMaxLength();
    }
    @SuppressLint("SetTextI18n")
    private void setListeners(){
        binding.logInFragmentButtonForgotPassword.setOnClickListener(view -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_logInFragment_to_forgotPasswordFragment);
        });
        binding.logInFragmentButtonCreateNewAccount.setOnClickListener(view -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_logInFragment_to_signUpFragment);
        });
        binding.logInFragmentButtonCheck.setOnClickListener(view -> {
            if (!binding.logInFragmentUsername.getText().toString().trim().contains(Constants.USERNAME_SIGN) && !binding.logInFragmentUsername.getText().toString().trim().isEmpty()){
                binding.logInFragmentUsername.setText(Constants.USERNAME_SIGN + binding.logInFragmentUsername.getText().toString().trim());
            }
            if (binding.logInFragmentUsername.getText().toString().trim().length() < 2) {
                ShowDialog.show(requireActivity(), getResources().getString(R.string.username_can_not_be_empty));
            }else if (!binding.logInFragmentUsername.getText().toString().trim().equals(binding.logInFragmentUsername.getText().toString().trim().toLowerCase())){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.username_must_be_in_lower_case));
            }else if (binding.logInFragmentUsername.getText().toString().trim().contains(" ")){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.username_must_be_without_spaces));
            }else if (binding.logInFragmentPassword.getText().toString().trim().isEmpty()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.password_can_not_be_empty));
            }else if (binding.logInFragmentPassword.getText().toString().trim().length() < Constants.MINIMUM_PASSWORD_LENGTH){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.password_is_too_short));
            }else {
                logIn();
            }
        });
    }
    private void logIn(){
        ShowLoading.show(requireActivity());
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.USERNAME, binding.logInFragmentUsername.getText().toString().trim())
                .whereEqualTo(Constants.PASSWORD, binding.logInFragmentPassword.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.IS_SIGNED_IN, true);
                        preferenceManager.putBoolean(Constants.STATUS, true);
                        preferenceManager.putString(Constants.USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.NAME, documentSnapshot.getString(Constants.NAME));
                        preferenceManager.putString(Constants.PHONE, documentSnapshot.getString(Constants.PHONE));
                        preferenceManager.putString(Constants.IMAGE_PROFILE, documentSnapshot.getString(Constants.IMAGE_PROFILE));
                        preferenceManager.putString(Constants.PASSWORD, documentSnapshot.getString(Constants.PASSWORD));
                        preferenceManager.putString(Constants.BIO, documentSnapshot.getString(Constants.BIO));
                        preferenceManager.putString(Constants.USERNAME, documentSnapshot.getString(Constants.USERNAME));
                        preferenceManager.putString(Constants.RECOVERY_CODE, documentSnapshot.getString(Constants.RECOVERY_CODE));
                        ShowLoading.dismissDialog();
                        Replace.replaceActivity(requireActivity(), new RecoveryCodeActivity(), true);
                    } else {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(requireActivity(), getResources().getString(R.string.we_could_not_find_this_account));
                    }
                }).addOnFailureListener(e -> {
                    ShowDialog.show(requireActivity(), getResources().getString(R.string.error));
                });
    }
    private void setMaxLength(){
        binding.logInFragmentUsername.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.USERNAME_MAX_LENGTH)});
        binding.logInFragmentPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.PASSWORD_MAX_LENGTH)});
    }
}