package com.glortest.messenger.activities.channels;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.core.view.GravityCompat;

import com.bumptech.glide.Glide;
import com.glortest.messenger.R;
import com.glortest.messenger.activities.BaseActivity;
import com.glortest.messenger.activities.MainActivity;
import com.glortest.messenger.activities.SettingsActivity;
import com.glortest.messenger.adapters.RecentChannelsAdapter;
import com.glortest.messenger.databinding.ChannelsListLayoutBinding;
import com.glortest.messenger.databinding.NavigationHeaderBinding;
import com.glortest.messenger.listeners.ChannelListener;
import com.glortest.messenger.models.ChannelModel;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowLoading;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelsListActivity extends BaseActivity implements ChannelListener {
    private ChannelsListLayoutBinding binding;
    private List<ChannelModel> conversations;
    private RecentChannelsAdapter recentChannelsAdapter;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChannelsListLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        conversations = new ArrayList<>();
        recentChannelsAdapter = new RecentChannelsAdapter(this, conversations, this);
        preferenceManager = new PreferenceManager(this);
    }
    private void initFunc(){
        setListeners();
        setAdapter();
        getToken();
        listenConversations();
    }
    private void setListeners(){
        binding.activityMainImageProfile.setOnClickListener(view -> {
            binding.activityMainDrawerLayout.openDrawer(GravityCompat.START);
        });
        binding.searchChats.setOnClickListener(view -> {
            Replace.replaceActivity(this, new FindChannelActivity(), false);
        });
        binding.activityChannelsNavigationView.setNavigationItemSelectedListener(item -> {
            binding.activityMainDrawerLayout.closeDrawer(GravityCompat.START);
            if(item.getItemId() == R.id.chats){
                Replace.replaceActivity(this, new MainActivity(), false);
            }
            else if (item.getItemId() == R.id.settings){
                Replace.replaceActivity(this, new SettingsActivity(), false);
            } else if (item.getItemId() == R.id.new_chanenl){
                Replace.replaceActivity(this, new CreateNewChannelActivity(), false);
            }
            return true;
        });
    }
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        recentChannelsAdapter.notifyDataSetChanged();
        Glide.with(this).load(preferenceManager.getString(Constants.IMAGE_PROFILE)).into(binding.activityMainImageProfile);
        NavigationHeaderBinding headerBinding = NavigationHeaderBinding.bind(binding.activityChannelsNavigationView.getHeaderView(0));
        headerBinding.headerName.setText(preferenceManager.getString(Constants.NAME));
        headerBinding.headerUsername.setText(preferenceManager.getString(Constants.USERNAME));
        Glide.with(this).load(preferenceManager.getString(Constants.IMAGE_PROFILE)).into(headerBinding.headerImageProfile);
        headerBinding.headerImageProfile.setOnClickListener(view -> {
            binding.activityMainDrawerLayout.closeDrawer(GravityCompat.START);
            Replace.replaceActivity(this, new SettingsActivity(), false);
        });
    }
    private void setAdapter(){
        binding.activityMainRecyclerView.setAdapter(recentChannelsAdapter);
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token){
        preferenceManager.putString(Constants.FCM_TOKEN, token);
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .document(preferenceManager.getString(Constants.USER_ID)).update(Constants.FCM_TOKEN, token);
    }
    private void setWarningVisibility(Boolean isVisible){
        if (isVisible){
            binding.activityMainWarningLayout.setVisibility(View.VISIBLE);
        }else {
            binding.activityMainWarningLayout.setVisibility(View.GONE);
        }
    }
    private void listenConversations(){
        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                .whereArrayContains(Constants.SUBSCRIBERS, preferenceManager.getString(Constants.USERNAME))
                .addSnapshotListener(eventListener);
    }
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        ShowLoading.show(this);
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange: value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ChannelModel chatMessage = new ChannelModel();

                    chatMessage.avatar = documentChange.getDocument().getString(Constants.IMAGE_PROFILE);
                    chatMessage.conversionName = documentChange.getDocument().getString(Constants.NAME);
                    chatMessage.conversionId = documentChange.getDocument().getString(Constants.ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.LAST_MESSAGE);
                    chatMessage.username = documentChange.getDocument().getString(Constants.USERNAME);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                    chatMessage.bio = documentChange.getDocument().getString(Constants.BIO);
                    chatMessage.members = (ArrayList<String>) documentChange.getDocument().get(Constants.SUBSCRIBERS);
                    chatMessage.admins = (ArrayList<String>) documentChange.getDocument().get(Constants.ADMINS);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < conversations.size();i++){
                        ArrayList<String> arr = (ArrayList<String>) documentChange.getDocument().get(Constants.SUBSCRIBERS);
                        if (arr.contains(preferenceManager.getString(Constants.USERNAME))){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                            break;
                        }else{
                            int ii = 0;
                            while(ii < conversations.size()){
                                if(conversations.get(ii).username.equals(documentChange.getDocument().getString(Constants.USERNAME))){
                                    break;
                                }
                                ii++;
                            }
                            conversations.remove(ii);
                        }
                    }
                }else if(documentChange.getType() == DocumentChange.Type.REMOVED){
                    int ii = 0;
                    while(ii < conversations.size()){
                        if(conversations.get(ii).username.equals(documentChange.getDocument().getString(Constants.USERNAME))){
                            break;
                        }
                        ii++;
                    }
                    conversations.remove(ii);
                }
            }
            setWarningVisibility(conversations.isEmpty());
            Collections.sort(conversations, (Object1, Object2) -> Object2.dateObject.compareTo(Object1.dateObject));
            recentChannelsAdapter.notifyDataSetChanged();
            binding.activityMainRecyclerView.scrollToPosition(0);
            ShowLoading.dismissDialog();
        }
    });


    @Override
    public void onChannelConversationClicked(ChannelModel channel) {
        Intent intent = new Intent(this, ChannelActivity.class);
        intent.putExtra(Constants.USER, channel);
        startActivity(intent);
    }
}