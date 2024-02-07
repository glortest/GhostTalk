package com.glortest.messenger.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.glortest.messenger.R;
import com.glortest.messenger.adapters.UsersAdapter;
import com.glortest.messenger.databinding.AddFriendLayoutBinding;
import com.glortest.messenger.listeners.UserListeners;
import com.glortest.messenger.models.User;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class AddFriendActivity extends BaseActivity implements UserListeners {
    private AddFriendLayoutBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AddFriendLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
        users = new ArrayList<>();
    }
    private void initFunc(){
        setListeners();
    }
    @SuppressLint("SetTextI18n")
    private void setListeners() {
        binding.addFriendActivityButtonBack.setOnClickListener(view -> onBackPressed());
        binding.addFriendActivityButtonFind.setOnClickListener(view -> {
            if (!binding.addFriendActivityUsername.getText().toString().trim().contains(Constants.USERNAME_SIGN) && !binding.addFriendActivityUsername.getText().toString().trim().isEmpty()){
                binding.addFriendActivityUsername.setText(Constants.USERNAME_SIGN + binding.addFriendActivityUsername.getText().toString().trim());
            }
            if (binding.addFriendActivityUsername.getText().toString().trim().length() < 2){
                ShowDialog.show(this, getResources().getString(R.string.username_can_not_be_empty));
            } else if (!binding.addFriendActivityUsername.getText().toString().trim().equals(binding.addFriendActivityUsername.getText().toString().trim().toLowerCase())){
                ShowDialog.show(this, getResources().getString(R.string.username_must_be_in_lower_case));
            } else if (binding.addFriendActivityUsername.getText().toString().trim().contains(" ")){
                ShowDialog.show(this, getResources().getString(R.string.username_must_be_without_spaces));
            }else {
                findFriend();
            }
        });
    }
    private void findFriend(){
        ShowLoading.show(this);
        users.clear();
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.USERNAME, binding.addFriendActivityUsername.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            if (preferenceManager.getString(Constants.USER_ID).equals(queryDocumentSnapshot.getId())){
                                ShowLoading.dismissDialog();
                            } else {
                                User user = new User();
                                user.name = queryDocumentSnapshot.getString(Constants.NAME);
                                user.phone = queryDocumentSnapshot.getString(Constants.USERNAME);
                                user.image = queryDocumentSnapshot.getString(Constants.IMAGE_PROFILE);
                                user.token = queryDocumentSnapshot.getString(Constants.FCM_TOKEN);
                                user.id = queryDocumentSnapshot.getId();
                                users.add(user);
                                ShowLoading.dismissDialog();
                            }
                        }
                        if (users.size() > 0){
                            UsersAdapter usersAdapter = new UsersAdapter(users, this, this);
                            binding.addFriendActivityRecyclerView.setAdapter(usersAdapter);
                        }
                    } else {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(this, getResources().getString(R.string.we_could_not_find_this_account));
                    }
                }).addOnFailureListener(e -> {
                    ShowLoading.dismissDialog();
                    ShowDialog.show(this, getResources().getString(R.string.error));
                });
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.USER, user);
        startActivity(intent);
        finish();
    }
}