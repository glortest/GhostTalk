package com.glortest.messenger.activities.channels;

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
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.glortest.messenger.R;
import com.glortest.messenger.activities.BaseActivity;
import com.glortest.messenger.databinding.CreateNewChannelLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class CreateNewChannelActivity extends BaseActivity {
    private CreateNewChannelLayoutBinding binding;
    private PreferenceManager preferenceManager;
    private Uri imageUri;
    private String downloadUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("eee", "11111111");
        binding = CreateNewChannelLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
    }
    private void initFunc(){
        setListeners();
        setMaxLength();
    }
    @SuppressLint("SetTextI18n")
    private void setListeners(){
        binding.signUpActivityButtonBack.setOnClickListener(view -> {
            this.onBackPressed();
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
                ShowDialog.show(this, getResources().getString(R.string.channel_name_can_not_be_empty));
            }else if(binding.signUpFragmentUsername.getText().toString().trim().length() < 2){
                ShowDialog.show(this, getResources().getString(R.string.channel_tag_can_not_be_empty));
            }else if (!binding.signUpFragmentUsername.getText().toString().trim().equals(binding.signUpFragmentUsername.getText().toString().trim().toLowerCase())){
                ShowDialog.show(this, getResources().getString(R.string.channel_tag_must_be_in_lower_case));
            }else if (binding.signUpFragmentUsername.getText().toString().trim().contains(" ")){
                ShowDialog.show(this, getResources().getString(R.string.channel_tag_must_be_without_spaces));
            }else if (binding.signUpFragmentAbout.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.channel_about_can_not_be_empty));
            }else if (binding.signUpFragmentAbout.getText().toString().trim().length() > Constants.BIO_MAX_LENGTH){
                ShowDialog.show(this, getResources().getString(R.string.channel_about_is_too_long));
            }else {
                signUp();
            }
        });
    }
    private void setMaxLength(){
        binding.signUpFragmentName.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.NAME_MAX_LENGTH)});
        binding.signUpFragmentUsername.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.USERNAME_MAX_LENGTH)});
        binding.signUpFragmentAbout.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.BIO_MAX_LENGTH)});
    }
    @SuppressLint("DefaultLocale")
    public static String getRecoveryCode() {
        Random random = new Random();
        int number = random.nextInt(99999999);

        return String.format("%08d", number);
    }
    private void signUp() {
        ShowLoading.show(this);
        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                .whereEqualTo(Constants.USERNAME, binding.signUpFragmentUsername.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        ShowLoading.dismissDialog();
                        ShowDialog.show(this, getResources().getString(R.string.this_channel_tag_is_already_linked_to_the_account));
                    } else {

                        HashMap<String, Object> conversation = new HashMap<>();
                        conversation.put(Constants.NAME, binding.signUpFragmentName.getText().toString().trim());
                        conversation.put(Constants.USERNAME, binding.signUpFragmentUsername.getText().toString().trim());
                        conversation.put(Constants.LAST_MESSAGE, "Канал создан");
                        conversation.put(Constants.IMAGE_PROFILE, downloadUri != null?downloadUri:Constants.DEFAULT_IMAGE_PROFILE);
                        conversation.put(Constants.TIMESTAMP, new Date());
                        conversation.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_TEXT);
                        conversation.put(Constants.BIO, binding.signUpFragmentAbout.getText().toString().trim());

                        ArrayList<String> admins = new ArrayList<>();
                        admins.add(preferenceManager.getString(Constants.USERNAME));
                        conversation.put(Constants.ADMINS, admins);

                        ArrayList<String> subs2 = new ArrayList<>();
                        subs2.add(preferenceManager.getString(Constants.USERNAME));
                        conversation.put(Constants.SUBSCRIBERS, subs2);

                        addConversation(conversation);

                        ShowLoading.dismissDialog();
                        Replace.replaceActivity(this, new ChannelsListActivity(), true);


                    }
                });
    }

    private void addConversation(HashMap<String, Object> conversation){
        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                .add(conversation);
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
    private void updateFirebase(){
        if (imageUri != null){
            ShowLoading.show(this);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PACKAGE + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        downloadUri = String.valueOf(uriTask.getResult());
                        binding.signUpFragmentImageProfileHelp.setVisibility(View.GONE);
                        Glide.with(this).load(imageUri).into(binding.signUpFragmentImageProfile);
                        ShowLoading.dismissDialog();
                    })
                    .addOnFailureListener(e -> {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(this, getResources().getString(R.string.error));
                    });
        }
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}