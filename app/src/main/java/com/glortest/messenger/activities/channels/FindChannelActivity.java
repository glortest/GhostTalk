package com.glortest.messenger.activities.channels;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.glortest.messenger.R;
import com.glortest.messenger.activities.BaseActivity;
import com.glortest.messenger.adapters.RecentChannelsAdapter;
import com.glortest.messenger.databinding.FindChannelLayoutBinding;
import com.glortest.messenger.listeners.ChannelListener;
import com.glortest.messenger.models.ChannelModel;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowDialog;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FindChannelActivity extends BaseActivity implements ChannelListener {
    private FindChannelLayoutBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChannelModel> conversations;
    private RecentChannelsAdapter recentChatAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FindChannelLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        conversations = new ArrayList<>();
        preferenceManager = new PreferenceManager(this);
        recentChatAdapter = new RecentChannelsAdapter(this, conversations, this);
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
                ShowDialog.show(this, getResources().getString(R.string.username_can_not_be_empty));
            }else {
                System.out.println(preferenceManager.getString(Constants.NAME));
                InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                        .whereEqualTo(Constants.USERNAME, binding.findChatActivityUsername.getText().toString().trim())
                        .addSnapshotListener(eventListener);

            }
        });
    }
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
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
            }

            System.out.println(conversations.size());
            Collections.sort(conversations, (Object1, Object2) -> Object2.dateObject.compareTo(Object1.dateObject));
            recentChatAdapter.notifyDataSetChanged();
            binding.findChatActivityRecyclerView.scrollToPosition(0);
        }
    }});

    @Override
    public void onChannelConversationClicked(ChannelModel channel) {
        Intent intent = new Intent(this, ChannelActivity.class);
        intent.putExtra(Constants.USER, channel);
        startActivity(intent);
        finish();
    }
}