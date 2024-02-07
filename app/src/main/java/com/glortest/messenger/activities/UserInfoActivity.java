package com.glortest.messenger.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

import com.glortest.messenger.databinding.ActivityUserInfoBinding;
import com.glortest.messenger.models.User;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowToast;


public class UserInfoActivity extends BaseActivity {
    private ActivityUserInfoBinding binding;
    private User receiverUser;
    private String imageProfile;
    private String userName;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
        receiverUser = (User) getIntent().getSerializableExtra(Constants.USER);
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

        binding.blockUser.setOnClickListener(view -> {
            System.out.println(111111);
            InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_BLOCKED)
                    .whereEqualTo(Constants.BLOCKER_ID, preferenceManager.getString(Constants.USER_ID))
                    .whereEqualTo(Constants.BLOCKED_ID, receiverUser.id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if(task.getResult().isEmpty()){
                                    HashMap<String, Object> message = new HashMap<>();
                                    message.put(Constants.BLOCKED_ID, receiverUser.id);
                                    message.put(Constants.BLOCKER_ID, preferenceManager.getString(Constants.USER_ID));
                                    InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_BLOCKED).add(message);
                                    Replace.replaceActivity(UserInfoActivity.this, new MainActivity(), false);
                                    ShowToast.show(getApplicationContext(), "User is blocked", true);
                                } else {
                                    InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_BLOCKED).document(task.getResult().getDocuments().get(0).getId()).delete();

                                    Replace.replaceActivity(UserInfoActivity.this, new MainActivity(), false);
                                    ShowToast.show(getApplicationContext(), "User is unblocked", true);
                                }
                            }
                        }
                    });
        });
    }
    private void setUserInfo(){
        InitFirebase.firebaseFirestore.collection(Constants.USERS).document(receiverUser.id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    imageProfile = documentSnapshot.getString(Constants.IMAGE_PROFILE);
                    userName = documentSnapshot.getString(Constants.NAME);
                    binding.userActivityTitle.setText(userName);
                    Glide.with(this).load(imageProfile).into(binding.userActivityHeaderImageProfile);
                    binding.userActivityButtonPhoneNumberText.setText(documentSnapshot.getString(Constants.USERNAME));
                    binding.userActivityButtonBioText.setText(documentSnapshot.getString(Constants.BIO));
                });
    }
}