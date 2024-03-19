package com.glortest.messenger.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import com.bumptech.glide.Glide;
import com.glortest.messenger.R;
import com.glortest.messenger.activities.channels.ChannelsListActivity;
import com.glortest.messenger.adapters.RecentChatAdapter;
import com.glortest.messenger.databinding.MainLayoutBinding;
import com.glortest.messenger.databinding.NavigationHeaderBinding;
import com.glortest.messenger.listeners.ConversationListener;
import com.glortest.messenger.models.ChatMessage;
import com.glortest.messenger.models.User;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowLoading;
import com.glortest.messenger.utilities.ShowToast;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends BaseActivity implements ConversationListener {
    private MainLayoutBinding binding;
    private List<ChatMessage> conversations;
    private RecentChatAdapter recentChatAdapter;
    private PreferenceManager preferenceManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
        }

        if (!isNetworkConnected()) {
            showDialog();
        }

        internetListener();
        initFields();
        initFunc();
    }
    private void initFields(){
        conversations = new ArrayList<>();
        recentChatAdapter = new RecentChatAdapter(this, conversations, this);
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
            Replace.replaceActivity(this, new FindChatActivity(), false);
        });
        binding.activityMainNavigationView.setNavigationItemSelectedListener(item -> {
            binding.activityMainDrawerLayout.closeDrawer(GravityCompat.START);
            if (item.getItemId() == R.id.settings){
                Replace.replaceActivity(this, new SettingsActivity(), false);
            } else if (item.getItemId() == R.id.friend){
                Replace.replaceActivity(this, new AddFriendActivity(), false);
            } else if (item.getItemId() == R.id.archive){
                if(preferenceManager.getString(Constants.ARCHIVE_PASSWORD) != null)
                    Replace.replaceActivity(this, new ArchivePasswordActivity(), false);
                else
                    Replace.replaceActivity(this, new ArchiveActivity(), false);
            }else if(item.getItemId() == R.id.contacts){
                if(preferenceManager.getString(Constants.PHONE) != null)
                    Replace.replaceActivity(this, new ContactsActivity(), false);
                else
                    ShowToast.show(this, getString(R.string.error_phone), true);
            }else if(item.getItemId() == R.id.channels_list){
                Replace.replaceActivity(this, new ChannelsListActivity(), false);
            }
            return true;
        });
    }
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        recentChatAdapter.notifyDataSetChanged();
        Glide.with(this).load(preferenceManager.getString(Constants.IMAGE_PROFILE)).into(binding.activityMainImageProfile);
        NavigationHeaderBinding headerBinding = NavigationHeaderBinding.bind(binding.activityMainNavigationView.getHeaderView(0));
        headerBinding.headerName.setText(preferenceManager.getString(Constants.NAME));
        headerBinding.headerUsername.setText(preferenceManager.getString(Constants.USERNAME));
        Glide.with(this).load(preferenceManager.getString(Constants.IMAGE_PROFILE)).into(headerBinding.headerImageProfile);
        headerBinding.headerImageProfile.setOnClickListener(view -> {
            binding.activityMainDrawerLayout.closeDrawer(GravityCompat.START);
            Replace.replaceActivity(this, new SettingsActivity(), false);
        });
    }
    private void setAdapter(){
        binding.activityMainRecyclerView.setAdapter(recentChatAdapter);
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
        InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                .whereEqualTo(Constants.IN_ARCHIVE, false)
                .addSnapshotListener(eventListener);
        InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
                .whereEqualTo(Constants.IN_ARCHIVE, false)
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
                    String senderId = documentChange.getDocument().getString(Constants.SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < conversations.size();i++){
                        String senderId = documentChange.getDocument().getString(Constants.SENDER_ID);
                        String receivedId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receivedId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                            break;
                        }
                    }
                } else if (documentChange.getType() == DocumentChange.Type.REMOVED){
                    int i = 0;
                    while(i < conversations.size()){
                        if(conversations.get(i).receiverId.equals(documentChange.getDocument().getString(Constants.RECEIVER_ID))
                        && conversations.get(i).senderId.equals(documentChange.getDocument().getString(Constants.SENDER_ID))){
                            break;
                        }
                        i++;
                    }
                    conversations.remove(i);
                }
            }
            setWarningVisibility(conversations.isEmpty());
            Collections.sort(conversations, (Object1, Object2) -> Object2.dateObject.compareTo(Object1.dateObject));
            recentChatAdapter.notifyDataSetChanged();
            binding.activityMainRecyclerView.scrollToPosition(0);
            ShowLoading.dismissDialog();
        }
    });

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.USER, user);
        startActivity(intent);
    }




    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void internetListener() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();

        connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {

            public void onAvailable(@NonNull Network network) {

            }

            public void onLost(@NonNull Network network) {
            }

            public void onUnavailable() {
            }
        });
    }

    private void showDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_no_internet);

        Button buttonDialogOk = dialog.findViewById(R.id.button_ok);
        buttonDialogOk.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }
}