package com.glortest.messenger.activities;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.glortest.messenger.R;
import com.glortest.messenger.databinding.ImageWatchLayoutBinding;
import com.glortest.messenger.utilities.Constants;

public class ImageWatchActivity extends BaseActivity {
    private ImageWatchLayoutBinding binding;
    private String userName;
    private String imageProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ImageWatchLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        userName = String.valueOf(R.string.image_wather);
        imageProfile = (String) getIntent().getSerializableExtra(Constants.ARGUMENT_IMAGE_PROFILE);
    }
    private void initFunc(){
        setListeners();
        setUserInfo();
    }
    private void setListeners(){
        binding.activityImageUserProfileButtonBack.setOnClickListener(view -> onBackPressed());
    }
    private void setUserInfo(){
        Glide.with(this).load(imageProfile).into(binding.activityImageUserImageProfile);
        binding.activityImageUserProfileTitle.setText(userName);
    }
}