package com.glortest.messenger.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;

import com.glortest.messenger.R;
import com.glortest.messenger.databinding.SetPhoneLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowToast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SetPhoneActivity extends BaseActivity {
    private SetPhoneLayoutBinding binding;
    private PreferenceManager preferenceManager;
    private String mVerificationId;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SetPhoneLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
    }
    private void initFunc(){
        setListeners();
        binding.setPhoneActivityPhone.requestFocus();
    }
    private void setListeners(){
        binding.setPhoneActivityCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(binding.activityChangePasswordButtonNext.getVisibility() == View.VISIBLE){
                    binding.activityChangePasswordButtonNext.setVisibility(View.GONE);
                    binding.activityChangePasswordButtonCheck.setVisibility(View.VISIBLE);
                }
            }
        });
        binding.activityChangePasswordButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityChangePasswordButtonNext.setOnClickListener(view -> {
            if (binding.setPhoneActivityPhone.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.phone_can_not_be_empty));
            } else if (binding.setPhoneActivityPhone.getText().toString().trim().length() < Constants.MINIMUM_PASSWORD_LENGTH){
                ShowDialog.show(this, getResources().getString(R.string.phone_is_too_short));
            } else {
                replaceBad();
                sendCode();
                binding.setPhoneActivityCode.requestFocus();
            }
        });

        binding.activityChangePasswordButtonCheck.setOnClickListener(view -> {
            String code = binding.setPhoneActivityCode.getText().toString();
            verifyPhoneNumberWithCode(mVerificationId, code);
        });
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            InitFirebase.firebaseFirestore.collection(Constants.USERS).document(preferenceManager.getString(Constants.USER_ID))
                                    .update(Constants.PHONE, binding.setPhoneActivityPhone.getText().toString());
                            preferenceManager.putString(Constants.PHONE, binding.setPhoneActivityPhone.getText().toString());
                            ShowToast.show(getApplicationContext(), "Successfully!", true);
                            onBackPressed();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                ShowToast.show(getApplicationContext(), "Invalid code", false);
                            }
                        }
                    }
                });
    }

    private void sendCode(){
        mAuth = FirebaseAuth.getInstance();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(binding.setPhoneActivityPhone.getText().toString())
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onCodeSent(String verificationId,
                                                   PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                                mVerificationId = verificationId;
                            }

                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {

                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    @SuppressLint("SetTextI18n")
    private void replaceBad(){
        binding.setPhoneActivityPhone.setText(
                binding.setPhoneActivityPhone.getText().toString()
                        .replace(" ", "")
                        .replace("(", "")
                        .replace(")", "")
                        .replace("-", "")
        );
    }
}