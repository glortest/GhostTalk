package com.glortest.messenger.activities.channels;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.glortest.messenger.activities.BaseActivity;
import com.glortest.messenger.activities.ImageUserActivity;
import com.glortest.messenger.databinding.ChannelAboutLayoutBinding;
import com.glortest.messenger.models.ChannelModel;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class ChannelAboutActivity extends BaseActivity {
    private ChannelAboutLayoutBinding binding;
    private ChannelModel receiverUser;
    private String imageProfile;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChannelAboutLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    @Override
    protected void onResume() {
        super.onResume();
        initFunc();
    }

    private void initFields(){
        receiverUser = (ChannelModel) getIntent().getSerializableExtra(Constants.COLLECTION_CHANNEL);
    }
    private void initFunc(){
        setListeners();
        setUserInfo();
    }
    private void setListeners(){
        binding.userActivityButtonBack.setOnClickListener(view -> onBackPressed());
        binding.userActivityHeaderImageProfile.setOnClickListener(view -> {
            Intent imageUser = new Intent(this, ImageUserActivity.class);
            imageUser.putExtra(Constants.ARGUMENT_IMAGE_PROFILE, imageProfile);
            imageUser.putExtra(Constants.ARGUMENT_NAME, userName);
            startActivity(imageUser);
        });


    }
    private void setUserInfo(){
        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                .whereEqualTo(Constants.USERNAME, receiverUser.username)
                .get()
                .addOnSuccessListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getDocuments().get(0);
                    imageProfile = documentSnapshot.getString(Constants.IMAGE_PROFILE);
                    userName = documentSnapshot.getString(Constants.NAME);
                    binding.userActivityTitle.setText(userName);
                    Glide.with(this).load(imageProfile).into(binding.userActivityHeaderImageProfile);
                    binding.userActivityButtonPhoneNumberText.setText(documentSnapshot.getString(Constants.USERNAME));
                    binding.userActivityButtonBioText.setText(documentSnapshot.getString(Constants.BIO));
                    ArrayList<String> subs = (ArrayList<String>) documentSnapshot.get(Constants.SUBSCRIBERS);
                    System.out.println(subs.size());
                    binding.userActivityButtonSubsText.setText(String.valueOf(subs.size()));
                });
    }
}