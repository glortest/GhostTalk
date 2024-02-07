package com.glortest.messenger.activities.channels;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.glortest.messenger.activities.BaseActivity;
import com.glortest.messenger.adapters.SubscribersAdapter;
import com.glortest.messenger.databinding.SubscribersListLayoutBinding;
import com.glortest.messenger.listeners.SubListener;
import com.glortest.messenger.models.ChannelModel;
import com.glortest.messenger.models.User;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowLoading;
import com.glortest.messenger.utilities.ShowToast;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SubscribersListActivity extends BaseActivity implements SubListener {
    private SubscribersListLayoutBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> conversations;
    private SubscribersAdapter recentChatAdapter;
    private ChannelModel channel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        channel = (ChannelModel) getIntent().getSerializableExtra(Constants.COLLECTION_CHANNEL);
        super.onCreate(savedInstanceState);
        binding = SubscribersListLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        conversations = new ArrayList<>();
        preferenceManager = new PreferenceManager(this);
        recentChatAdapter = new SubscribersAdapter(this, conversations, this);
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
        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                .whereEqualTo(Constants.USERNAME, channel.username)
                .get().addOnSuccessListener(task ->{
                    ArrayList<String> arr = (ArrayList<String>) task.getDocuments().get(0).get(Constants.SUBSCRIBERS);
                    for(String user: arr){
                        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                                .whereEqualTo(Constants.USERNAME, user)
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
                    InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS).whereEqualTo(Constants.USERNAME, channel.username).get()
                            .addOnSuccessListener(task -> {
                                User chatMessage = new User();
                                chatMessage.admins = (ArrayList<String>) task.getDocuments().get(0).get(Constants.ADMINS);


                                String username = documentChange.getDocument().getString(Constants.USERNAME);
                                String image = documentChange.getDocument().getString(Constants.IMAGE_PROFILE);
                                String name = documentChange.getDocument().getString(Constants.NAME);
                                chatMessage.username = username;
                                chatMessage.image = image;
                                chatMessage.name = name;
                                if (!preferenceManager.getString(Constants.USERNAME).equals(username)){
                                    conversations.add(chatMessage);
                                }

                                System.out.println(conversations.size());
                                recentChatAdapter.notifyDataSetChanged();
                                binding.findChatActivityRecyclerView.scrollToPosition(0);
                                ShowLoading.dismissDialog();
                            });


                }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS).whereEqualTo(Constants.USERNAME, channel.username).get()
                            .addOnSuccessListener(task -> {
                                User chatMessage = new User();
                                chatMessage.admins = (ArrayList<String>) task.getDocuments().get(0).get(Constants.ADMINS);


                                String username = documentChange.getDocument().getString(Constants.USERNAME);
                                String image = documentChange.getDocument().getString(Constants.IMAGE_PROFILE);
                                String name = documentChange.getDocument().getString(Constants.NAME);
                                chatMessage.username = username;
                                chatMessage.image = image;
                                chatMessage.name = name;
                                if (!preferenceManager.getString(Constants.USERNAME).equals(username)){
                                    conversations.add(chatMessage);
                                }

                                System.out.println(conversations.size());
                                recentChatAdapter.notifyDataSetChanged();
                                binding.findChatActivityRecyclerView.scrollToPosition(0);
                                ShowLoading.dismissDialog();
                            });
                }
            }

        }
    });


    @Override
    public void onConversationClicked(User user, String type) {
        conversations.remove(user);
        if(type.equals("admin")){
            InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                    .whereEqualTo(Constants.USERNAME, channel.username)
                    .get()
                    .addOnSuccessListener(task ->{
                        ArrayList<String> admins = (ArrayList<String>) task.getDocuments().get(0).get(Constants.ADMINS);
                        admins.add(user.username);
                        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                                .document(task.getDocuments().get(0).getId())
                                .update(Constants.ADMINS, admins);

                        user.admins = admins;
                        conversations.add(user);
                        recentChatAdapter.notifyDataSetChanged();
                        binding.findChatActivityRecyclerView.scrollToPosition(0);
                    });
            ShowToast.show(this, "This user is admin now", true);
        }else if(type.equals("kick")){
            conversations.remove(user);
            InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                    .whereEqualTo(Constants.USERNAME, channel.username)
                    .get()
                    .addOnSuccessListener(task ->{
                        ArrayList<String> admins = (ArrayList<String>) task.getDocuments().get(0).get(Constants.SUBSCRIBERS);
                        admins.remove(user.username);
                        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                                .document(task.getDocuments().get(0).getId())
                                .update(Constants.SUBSCRIBERS, admins);


                    });
            recentChatAdapter.notifyDataSetChanged();
            binding.findChatActivityRecyclerView.scrollToPosition(0);
            ShowToast.show(this, "This user was kicked", true);
        }else if(type.equals("unadmin")){
            conversations.remove(user);
            InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                    .whereEqualTo(Constants.USERNAME, channel.username)
                    .get()
                    .addOnSuccessListener(task ->{
                        ArrayList<String> admins = (ArrayList<String>) task.getDocuments().get(0).get(Constants.ADMINS);
                        admins.remove(user.username);
                        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                                .document(task.getDocuments().get(0).getId())
                                .update(Constants.ADMINS, admins);


                        user.admins = admins;
                        conversations.add(user);
                        recentChatAdapter.notifyDataSetChanged();
                        binding.findChatActivityRecyclerView.scrollToPosition(0);
                    });
            ShowToast.show(this, "This user isn't admin now", true);
        }
    }
}