package com.glortest.messenger.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.glortest.messenger.R;
import com.glortest.messenger.activities.RecoveryCodeActivity;
import com.glortest.messenger.databinding.SignUpLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Random;

public class SignUpFragment extends Fragment {
    private SignUpLayoutBinding binding;
    private PreferenceManager preferenceManager;
    private Uri imageUri;
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = SignUpLayoutBinding.inflate(getLayoutInflater());
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
        preferenceManager.putString(Constants.IMAGE_PROFILE, Constants.DEFAULT_IMAGE_PROFILE);
    }
    @SuppressLint("SetTextI18n")
    private void setListeners(){
        binding.logInFragmentButtonBackToLogIn.setOnClickListener(view -> {
            requireActivity().onBackPressed();
        });
        binding.signUpFragmentName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                binding.signUpFragmentUsername.setText(Constants.USERNAME_SIGN + binding.signUpFragmentName.getText().toString().trim().toLowerCase());
            }
        });
        binding.signUpFragmentImageProfileLayout.setOnClickListener(view -> showDialogImage());
        binding.signUpFragmentButtonCheck.setOnClickListener(view -> {
            if (!binding.signUpFragmentUsername.getText().toString().trim().contains(Constants.USERNAME_SIGN) && !binding.signUpFragmentUsername.getText().toString().trim().isEmpty()){
                binding.signUpFragmentUsername.setText(Constants.USERNAME_SIGN + binding.signUpFragmentUsername.getText().toString().trim());
            }
            if (binding.signUpFragmentName.getText().toString().trim().isEmpty()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.name_can_not_be_empty));
            }else if(binding.signUpFragmentUsername.getText().toString().trim().length() < 2){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.username_can_not_be_empty));
            }else if (!binding.signUpFragmentUsername.getText().toString().trim().equals(binding.signUpFragmentUsername.getText().toString().trim().toLowerCase())){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.username_must_be_in_lower_case));
            }else if (binding.signUpFragmentUsername.getText().toString().trim().contains(" ")){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.username_must_be_without_spaces));
            }else if (binding.signUpFragmentPassword.getText().toString().trim().isEmpty()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.password_can_not_be_empty));
            }else if (binding.signUpFragmentPassword.getText().toString().trim().length() < Constants.MINIMUM_PASSWORD_LENGTH){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.password_is_too_short));
            }else if (!binding.signUpFragmentPassword.getText().toString().trim().equals(binding.signUpFragmentConfirmPassword.getText().toString().trim())){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.passwords_must_match));
            }else {
                signUp();
            }
        });
    }
    private void setMaxLength(){
        binding.signUpFragmentName.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.NAME_MAX_LENGTH)});
        binding.signUpFragmentUsername.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.USERNAME_MAX_LENGTH)});
        binding.signUpFragmentPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.PASSWORD_MAX_LENGTH)});
        binding.signUpFragmentConfirmPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.PASSWORD_MAX_LENGTH)});
    }
    @SuppressLint("DefaultLocale")
    public static String getRecoveryCode() {
        Random random = new Random();
        int number = random.nextInt(99999999);

        return String.format("%08d", number);
    }
    private void signUp() {
        ShowLoading.show(requireActivity());
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.USERNAME, binding.signUpFragmentUsername.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        ShowLoading.dismissDialog();
                        ShowDialog.show(requireActivity(), getResources().getString(R.string.this_username_is_already_linked_to_the_account));
                    } else {
                        String recoveryCode = getRecoveryCode();
                        HashMap<String, Object> user = new HashMap<>();
                        user.put(Constants.NAME, binding.signUpFragmentName.getText().toString().trim());
                        user.put(Constants.USERNAME, binding.signUpFragmentUsername.getText().toString().trim());
                        user.put(Constants.PASSWORD, binding.signUpFragmentPassword.getText().toString().trim());
                        user.put(Constants.IMAGE_PROFILE, preferenceManager.getString(Constants.IMAGE_PROFILE));
                        user.put(Constants.BIO, Constants.DEFAULT_BIO);
                        user.put(Constants.RECOVERY_CODE, recoveryCode);
                        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                                .add(user)
                                .addOnSuccessListener(documentReference -> {
                                    preferenceManager.putString(Constants.USER_ID, documentReference.getId());
                                    preferenceManager.putBoolean(Constants.IS_SIGNED_IN, true);
                                    preferenceManager.putBoolean(Constants.STATUS, true);
                                    preferenceManager.putString(Constants.NAME, binding.signUpFragmentName.getText().toString().trim());
                                    preferenceManager.putString(Constants.USERNAME, binding.signUpFragmentUsername.getText().toString().trim());
                                    preferenceManager.putString(Constants.PASSWORD, binding.signUpFragmentPassword.getText().toString().trim());
                                    preferenceManager.putString(Constants.BIO, Constants.DEFAULT_BIO);
                                    preferenceManager.putString(Constants.RECOVERY_CODE, recoveryCode);
                                    ShowLoading.dismissDialog();
                                    Replace.replaceActivity(requireActivity(), new RecoveryCodeActivity(), true);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("eeee", e.toString());
                                    ShowLoading.dismissDialog();
                                    ShowDialog.show(requireActivity(), getResources().getString(R.string.error));
                                });
                    }
                });
    }
    private void showDialogImage(){
        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.dialog_image);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button showDialogImagePhotoCamera = dialog.findViewById(R.id.showDialogImageButtonCamera);
        Button showDialogImageGallery = dialog.findViewById(R.id.showDialogImageButtonGallery);

        showDialogImagePhotoCamera.setOnClickListener(view -> dialog.dismiss());
        showDialogImageGallery.setOnClickListener(view -> {
            dialog.dismiss();
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        dialog.setCancelable(true);
        dialog.create();
        dialog.show();
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK){
                    if (result.getData() != null){
                        imageUri = result.getData().getData();
                        updateFirebase();
                    }
                }
            }
    );
    private void updateFirebase(){
        if (imageUri != null){
            ShowLoading.show(requireActivity());
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PACKAGE + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        final String downloadUri = String.valueOf(uriTask.getResult());
                        preferenceManager.putString(Constants.IMAGE_PROFILE, downloadUri);
                        binding.signUpFragmentImageProfileHelp.setVisibility(View.GONE);
                        Glide.with(this).load(imageUri).into(binding.signUpFragmentImageProfile);
                        ShowLoading.dismissDialog();
                    })
                    .addOnFailureListener(e -> {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(requireActivity(), getResources().getString(R.string.error));
                    });
        }
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = requireActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}