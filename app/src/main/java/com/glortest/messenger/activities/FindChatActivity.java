package com.glortest.messenger.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.glortest.messenger.R;
import com.glortest.messenger.adapters.RecentChatAdapter;
import com.glortest.messenger.databinding.FindChatLayoutBinding;
import com.glortest.messenger.listeners.ConversationListener;
import com.glortest.messenger.models.ChatMessage;
import com.glortest.messenger.models.User;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FindChatActivity extends BaseActivity implements ConversationListener {
    private FindChatLayoutBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentChatAdapter recentChatAdapter;
    private List<User> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FindChatLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        conversations = new ArrayList<>();
        preferenceManager = new PreferenceManager(this);
        recentChatAdapter = new RecentChatAdapter(this, conversations, this);
        users = new ArrayList<>();
    }

    private void setAdapter(){
        binding.findChatActivityRecyclerView.setAdapter(recentChatAdapter);
    }


    private void initFunc(){
        setListeners();
        setAdapter();
    }


    @SuppressLint("SetTextI18n")
    private void setListeners() {
        binding.findChatActivityButtonBack.setOnClickListener(view -> onBackPressed());
        binding.findChatActivityButtonFind.setOnClickListener(view -> {
            if (!binding.findChatActivityUsername.getText().toString().trim().contains(Constants.USERNAME_SIGN) && !binding.findChatActivityUsername.getText().toString().trim().isEmpty()){
                binding.findChatActivityUsername.setText(binding.findChatActivityUsername.getText().toString().trim());
            }
            if (binding.findChatActivityUsername.getText().toString().trim().length() < 2){
                ShowDialog.show(this, getResources().getString(R.string.nickname_can_not_be_empty));
            }else {
                System.out.println(preferenceManager.getString(Constants.NAME));
                InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                        .whereEqualTo("receiverName", binding.findChatActivityUsername.getText().toString().trim())
                        .whereEqualTo("senderName", preferenceManager.getString(Constants.NAME))
                        .addSnapshotListener(eventListener);
                InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                        .whereEqualTo("receiverName", preferenceManager.getString(Constants.NAME))
                        .whereEqualTo("senderName", binding.findChatActivityUsername.getText().toString().trim())
                        .addSnapshotListener(eventListener);

            }
        });
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
                    System.out.println(senderId);
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
                }
            }

            System.out.println(conversations.size());
            Collections.sort(conversations, (Object1, Object2) -> Object2.dateObject.compareTo(Object1.dateObject));
            recentChatAdapter.notifyDataSetChanged();
            binding.findChatActivityRecyclerView.scrollToPosition(0);
            ShowLoading.dismissDialog();
        }
    });



    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.USER, user);
        startActivity(intent);
        finish();
    }
}