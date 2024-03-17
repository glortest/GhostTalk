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
import com.glortest.messenger.databinding.ForgotPasswordLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;


public class ForgotPasswordFragment extends Fragment {
    private ForgotPasswordLayoutBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = ForgotPasswordLayoutBinding.inflate(getLayoutInflater());
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
    }
    @SuppressLint("SetTextI18n")
    private void setListeners(){
        binding.fragmentForgotPasswordButtonBack.setOnClickListener(view -> requireActivity().onBackPressed());
        binding.fragmentForgotPasswordButtonCheck.setOnClickListener(view -> {
            if (!binding.fragmentForgotPasswordUsername.getText().toString().trim().contains(Constants.USERNAME_SIGN) && !binding.fragmentForgotPasswordUsername.getText().toString().trim().isEmpty()){
                binding.fragmentForgotPasswordUsername.setText(Constants.USERNAME_SIGN + binding.fragmentForgotPasswordUsername.getText().toString().trim());
            }
            if (binding.fragmentForgotPasswordUsername.getText().toString().trim().length() < 2){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.nickname_can_not_be_empty));
            }else if (!binding.fragmentForgotPasswordUsername.getText().toString().trim().equals(binding.fragmentForgotPasswordUsername.getText().toString().trim().toLowerCase())){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.nickname_must_be_in_lower_case));
            }else if (binding.fragmentForgotPasswordUsername.getText().toString().trim().contains(" ")){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.nickname_must_be_without_spaces));
            }else if (binding.fragmentForgotPasswordRecoveryCode.getText().toString().trim().isEmpty()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.recovery_code_can_not_be_empty));
            }else if (binding.fragmentForgotPasswordRecoveryCode.getText().toString().trim().length() < Constants.RECOVERY_CODE_MAX_LENGTH) {
                ShowDialog.show(requireActivity(), getString(R.string.recovery_code_must_contain_8_digits));
            }else {
                logIn();
            }
        });
    }
    private void logIn(){
        ShowLoading.show(requireActivity());
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.USERNAME, binding.fragmentForgotPasswordUsername.getText().toString().trim())
                .whereEqualTo(Constants.RECOVERY_CODE, binding.fragmentForgotPasswordRecoveryCode.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putString(Constants.USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.NAME, documentSnapshot.getString(Constants.NAME));
                        preferenceManager.putString(Constants.IMAGE_PROFILE, documentSnapshot.getString(Constants.IMAGE_PROFILE));
                        preferenceManager.putString(Constants.PASSWORD, documentSnapshot.getString(Constants.PASSWORD));
                        preferenceManager.putString(Constants.BIO, documentSnapshot.getString(Constants.BIO));
                        preferenceManager.putString(Constants.USERNAME, documentSnapshot.getString(Constants.USERNAME));
                        preferenceManager.putString(Constants.RECOVERY_CODE, documentSnapshot.getString(Constants.RECOVERY_CODE));
                        ShowLoading.dismissDialog();
                        Navigation.findNavController(requireView()).navigate(R.id.action_forgotPasswordFragment_to_newPasswordFragment);
                    } else {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(requireActivity(), getResources().getString(R.string.we_could_not_find_this_account));
                    }
                });
    }
    private void setMaxLength(){
        binding.fragmentForgotPasswordUsername.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.USERNAME_MAX_LENGTH)});
        binding.fragmentForgotPasswordRecoveryCode.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.RECOVERY_CODE_MAX_LENGTH)});
    }
}