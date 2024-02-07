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
import com.glortest.messenger.databinding.ImageProfileLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.glortest.messenger.utilities.ShowToast;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ImageProfileActivity extends BaseActivity {
    private ImageProfileLayoutBinding binding;
    private PreferenceManager preferenceManager;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ImageProfileLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
    }
    private void initFunc(){
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Glide.with(this).load(preferenceManager.getString(Constants.IMAGE_PROFILE)).into(binding.activityImageImageProfile);
    }

    private void setListeners(){
        binding.activityImageProfileButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityImageProfileButtonEdit.setOnClickListener(view -> showDialogImage());
    }
    private void showDialogImage(){
        Dialog dialog = new Dialog(this);
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
                                    Glide.with(this).load(downloadUri).into(binding.activityImageImageProfile);
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