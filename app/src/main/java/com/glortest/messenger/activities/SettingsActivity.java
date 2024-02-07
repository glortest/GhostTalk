package com.glortest.messenger.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.glortest.messenger.R;
import com.glortest.messenger.databinding.SettingsLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.glortest.messenger.utilities.ShowToast;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;


public class SettingsActivity extends BaseActivity {
    private SettingsLayoutBinding binding;
    private Uri imageUri;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SettingsLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
    }
    private void initFunc(){
        setListeners();
        setSwitchStatus();
    }
    @Override
    public void onResume() {
        super.onResume();
        binding.settingsActivityButtonRecoveryCodeText.setText(preferenceManager.getString(Constants.RECOVERY_CODE));
        binding.settingsActivityButtonNameText.setText(preferenceManager.getString(Constants.NAME));
        binding.settingsActivityButtonPhoneNumberText.setText(preferenceManager.getString(Constants.USERNAME));
        binding.settingsActivityButtonBioText.setText(preferenceManager.getString(Constants.BIO));
        binding.settingsActivityButtonPasswordText.setText(preferenceManager.getString(Constants.PASSWORD));
        binding.settingsActivityArchivePasswordText.setText(preferenceManager.getString(Constants.ARCHIVE_PASSWORD));
        binding.settingsActivityAccountPhoneText.setText(preferenceManager.getString(Constants.PHONE));
        Glide.with(this).load(preferenceManager.getString(Constants.IMAGE_PROFILE)).into(binding.settingsActivityHeaderImageProfile);
    }
    private void setSwitchStatus(){
        if (preferenceManager.getBoolean(Constants.STATUS)){
            binding.settingsActivityButtonSwitchStatus.setChecked(preferenceManager.getBoolean(Constants.STATUS));
            binding.settingsActivityButtonOnlineText.setText(getResources().getString(R.string.online));
            binding.settingsActivityButtonOnlineText.setTextColor(getResources().getColor(R.color.icon_orange));
            InitFirebase.firebaseFirestore.collection(Constants.USERS).document(preferenceManager.getString(Constants.USER_ID))
                    .update(Constants.AVAILABLE, 1);
        } else {
            binding.settingsActivityButtonSwitchStatus.setChecked(preferenceManager.getBoolean(Constants.STATUS));
            binding.settingsActivityButtonOnlineText.setText(getResources().getString(R.string.offline));
            binding.settingsActivityButtonOnlineText.setTextColor(getResources().getColor(R.color.text_hint));
            InitFirebase.firebaseFirestore.collection(Constants.USERS).document(preferenceManager.getString(Constants.USER_ID))
                    .update(Constants.AVAILABLE, 0);
        }
    }
    private void setListeners(){
        binding.settingsActivityButtonSwitchStatus.setOnCheckedChangeListener((compoundButton, b) -> {
            preferenceManager.putBoolean(Constants.STATUS, b);
            setSwitchStatus();
        });
        binding.settingsActivityButtonPassword.setOnClickListener(view -> {
            Intent intent = new Intent(this, CheckPasswordActivity.class);
            intent.putExtra(Constants.ARGUMENT_PASSWORD, true);
            startActivity(intent);
        });
        binding.settingsActivityButtonRecoveryCode.setOnClickListener(view -> {
            Intent intent = new Intent(this, CheckPasswordActivity.class);
            intent.putExtra(Constants.ARGUMENT_PASSWORD, false);
            startActivity(intent);
        });
        binding.settingsActivityButtonBack.setOnClickListener(view -> onBackPressed());
        binding.settingsActivityButtonUsername.setOnClickListener(view -> Replace.replaceActivity(this, new ChangeUsernameActivity(), false));
        binding.settingsActivityButtonBio.setOnClickListener(view -> Replace.replaceActivity(this, new ChangeBioActivity(), false));
        binding.settingsActivityHeaderImageProfile.setOnClickListener(view -> Replace.replaceActivity(this, new ImageProfileActivity(), false));
        binding.settingsActivityHeaderChooseImageProfile.setOnClickListener(view -> showDialogImage());
        binding.settingsActivityButtonName.setOnClickListener(view -> Replace.replaceActivity(this, new ChangeNameActivity(), false));
        binding.settingActivityButtonSignOutButton.setOnClickListener(view -> showDialogSignOut());
        binding.settingsActivityArchivePassword.setOnClickListener(view -> Replace.replaceActivity(this, new ChangeArchivePasswordActivity(), false));
        binding.settingsActivityAccountPhone.setOnClickListener(view -> Replace.replaceActivity(this, new SetPhoneActivity(), false));
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
    private void showDialogSignOut(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_signout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button showDialogSignOutButtonYes = dialog.findViewById(R.id.showDialogSignOutButtonYes);

        showDialogSignOutButtonYes.setOnClickListener(view -> {
            dialog.dismiss();
            ShowLoading.show(this);
            DocumentReference documentReference = InitFirebase.firebaseFirestore.collection(Constants.USERS)
                    .document(preferenceManager.getString(Constants.USER_ID));
            HashMap<String, Object> updates = new HashMap<>();
            updates.put(Constants.FCM_TOKEN, FieldValue.delete());
            documentReference.update(updates)
                    .addOnSuccessListener(unused -> {
                        ShowLoading.dismissDialog();
                        preferenceManager.clear();
                        Replace.replaceActivity(this, new RegistrationActivity(), true);
                    }).addOnFailureListener(e -> {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(this, getResources().getString(R.string.error));
                    });

        });
        dialog.setCancelable(true);
        dialog.create();
        dialog.show();
    }
    private void showDialogImage(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_image);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button showDialogImagePhotoCamera = dialog.findViewById(R.id.showDialogImageButtonCamera);
        Button showDialogImageGallery = dialog.findViewById(R.id.showDialogImageButtonGallery);

        showDialogImagePhotoCamera.setOnClickListener(view -> {
            dialog.dismiss();
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pickImage.launch(cameraIntent);

        });
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
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void updateFirebase(){
        if (imageUri != null){
            ShowLoading.show(this);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PACKAGE + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        final String downloadUri = String.valueOf(uriTask.getResult());
                        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                                .document(preferenceManager.getString(Constants.USER_ID)).update(Constants.IMAGE_PROFILE, downloadUri)
                                .addOnSuccessListener(unused -> {
                                    preferenceManager.putString(Constants.IMAGE_PROFILE, downloadUri);
                                    Glide.with(this).load(downloadUri).into(binding.settingsActivityHeaderImageProfile);
                                    ShowLoading.dismissDialog();
                                    ShowToast.show(this, getResources().getString(R.string.profile_photo_updated_successfully), false);
                                }).addOnFailureListener(e -> {
                                    ShowLoading.dismissDialog();
                                    ShowDialog.show(this, getResources().getString(R.string.error));
                                });
                    })
                    .addOnFailureListener(e -> {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(this, getResources().getString(R.string.error));
                    });
        }
    }
}