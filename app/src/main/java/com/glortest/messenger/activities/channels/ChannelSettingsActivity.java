package com.glortest.messenger.activities.channels;

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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.glortest.messenger.R;
import com.glortest.messenger.activities.BaseActivity;
import com.glortest.messenger.activities.ImageProfileActivity;
import com.glortest.messenger.databinding.ChannelSettingsLayoutBinding;
import com.glortest.messenger.models.ChannelModel;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ChannelSettingsActivity extends BaseActivity {
    private ChannelSettingsLayoutBinding binding;
    private Uri imageUri;
    private PreferenceManager preferenceManager;
    private ChannelModel channel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        channel = (ChannelModel) getIntent().getSerializableExtra(Constants.COLLECTION_CHANNEL);
        super.onCreate(savedInstanceState);
        binding = ChannelSettingsLayoutBinding.inflate(getLayoutInflater());
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
    public void onResume() {
        super.onResume();
        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                .whereEqualTo(Constants.USERNAME, channel.username)
                .get()
                .addOnSuccessListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getDocuments().get(0);
                    binding.settingsActivityButtonNameText.setText(documentSnapshot.getString(Constants.NAME));
                    binding.settingsActivityButtonPhoneNumberText.setText(documentSnapshot.getString(Constants.USERNAME));
                    binding.settingsActivityButtonBioText.setText(documentSnapshot.getString(Constants.BIO));
                    binding.userActivityButtonSubsText.setText(String.valueOf(((ArrayList<?>) documentSnapshot.get(Constants.SUBSCRIBERS)).size()));
                    binding.userActivityButtonAdminsText.setText(String.valueOf(((ArrayList<?>) documentSnapshot.get(Constants.ADMINS)).size()));
                    Glide.with(this).load(documentSnapshot.get(Constants.IMAGE_PROFILE)).into(binding.settingsActivityHeaderImageProfile);

                });
    }
    private void setListeners(){
        binding.settingsActivityButtonBack.setOnClickListener(view -> onBackPressed());
        binding.settingsActivityButtonUsername.setOnClickListener(view -> Toast.makeText(this, R.string.channel_nickname_is_not_changeable, Toast.LENGTH_LONG));
        binding.settingsActivityButtonBio.setOnClickListener(view -> {
            Intent intent = new Intent(this, ChangeChannelBioActivity.class);
            intent.putExtra(Constants.COLLECTION_CHANNEL, channel);
            startActivity(intent);
        });
        binding.settingsActivityHeaderImageProfile.setOnClickListener(view -> Replace.replaceActivity(this, new ImageProfileActivity(), false));
        binding.settingsActivityHeaderChooseImageProfile.setOnClickListener(view -> showDialogImage());
        binding.settingsActivityButtonName.setOnClickListener(view -> {
                    Intent intent2 = new Intent(this, ChangeChannelNameActivity.class);
                    intent2.putExtra(Constants.COLLECTION_CHANNEL, channel);
                    startActivity(intent2);
                });

        binding.userActivityButtonSubs.setOnClickListener(view ->{
            Intent intent2 = new Intent(this, SubscribersListActivity.class);
            intent2.putExtra(Constants.COLLECTION_CHANNEL, channel);
            startActivity(intent2);
        });
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
                        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                                .whereEqualTo(Constants.USERNAME, channel.username)
                                .get().addOnSuccessListener(task ->{
                                    InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                                            .document(task.getDocuments().get(0).getId())
                                            .update(Constants.IMAGE_PROFILE, downloadUri)
                                            .addOnFailureListener(e -> {
                                                ShowLoading.dismissDialog();
                                                ShowDialog.show(this, getResources().getString(R.string.error));
                                            });
                                });


                    });
        }
    }
}