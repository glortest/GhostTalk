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
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.glortest.messenger.R;
import com.glortest.messenger.activities.BaseActivity;
import com.glortest.messenger.adapters.ChannelAdapter;
import com.glortest.messenger.databinding.ChannelLayoutBinding;
import com.glortest.messenger.models.ChannelModel;
import com.glortest.messenger.models.ChatMessage;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChannelActivity extends BaseActivity {
    private ChannelLayoutBinding binding;
    private ChannelModel receiverUser;
    private List<ChatMessage> chatMessages;
    private ChannelAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private String conversationId;
    private Boolean isReceiverAvailable;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChannelLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFields();
        initFunc();
    }
    private void initFields(){
        receiverUser = (ChannelModel) getIntent().getSerializableExtra(Constants.USER);
        preferenceManager = new PreferenceManager(this);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChannelAdapter(
                chatMessages,
                receiverUser.image,
                preferenceManager.getString(Constants.USER_ID),
                this
        );
        conversationId = null;
        isReceiverAvailable = false;
    }
    private void initFunc() {
        setListeners();
        setUserInfo();
        if (conversationId == null){
            checkForConversationRemotely(
                    binding.activityChatUsername.getText().toString()
            );
        }
        setAdapter();
        isBlocked();
        setSendButton();
        listenMessages();
    }
    private void setUserInfo(){
        binding.activityChatUserName.setText(receiverUser.name);
        binding.activityChatUsername.setText(receiverUser.username);
        binding.activityChatUsername.setVisibility(View.VISIBLE);
        Glide.with(this).load(receiverUser.avatar).into(binding.activityChatImageProfile);
    }
    @Override
    protected void onResume() {
        super.onResume();
        isBlocked();
    }

    private void setAdapter(){
        binding.activityChatsRecyclerView.setAdapter(chatAdapter);
    }
    private void setSendButton(){
        if (binding.activityChatsMessage.getText().toString().trim().isEmpty()){
            binding.activityChatsButtonSend.setVisibility(View.GONE);
        }else {
            binding.activityChatsButtonSend.setVisibility(View.VISIBLE);
            binding.activityChatsButtonAttach.setVisibility(View.GONE);
        }
    }

    private void isBlocked(){
        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                .whereEqualTo(Constants.USERNAME, binding.activityChatUsername.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean find = false;
                            for(String username: (ArrayList<String>)task.getResult().getDocuments().get(0).get(Constants.ADMINS)){
                                if(username.equals(preferenceManager.getString(Constants.USERNAME))){
                                    find=true;
                                }
                            }
                            if(!find){
                                binding.blockedView.setVisibility(View.VISIBLE);
                                binding.sendView.setVisibility(View.INVISIBLE);
                            }else{
                                binding.sendView.setVisibility(View.VISIBLE);
                                binding.blockedView.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });
    }


    private void setListeners(){
        binding.activityChatUser.setOnClickListener(view -> {
            Intent userActivity = new Intent(this, ChannelAboutActivity.class);
            userActivity.putExtra(Constants.COLLECTION_CHANNEL, receiverUser);
            startActivity(userActivity);
        });
        binding.activityChatsMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setSendButton();
            }
        });
        binding.activityChatsButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityChatsButtonSend.setOnClickListener(view -> sendMessage());
        binding.imageOther.setOnClickListener(view -> showDialog());

        binding.activityChatsButtonAttach.setOnClickListener(view -> sendAttach());
    }

    private void showDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_channel_action);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button showDialogChatButtonLeave = dialog.findViewById(R.id.showDialogChatButtonLeave);
        Button showDialogChatButtonJoin = dialog.findViewById(R.id.showDialogChatButtonJoin);
        Button showDialogChatButtonSettings = dialog.findViewById(R.id.showDialogChatButtonSettings);

        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                .whereEqualTo(Constants.USERNAME, binding.activityChatUsername.getText().toString())
                .get()
                .addOnSuccessListener(task -> {
                    ArrayList<String> arr = (ArrayList<String>) task.getDocuments().get(0).get(Constants.SUBSCRIBERS);
                    ArrayList<String> admins = (ArrayList<String>) task.getDocuments().get(0).get(Constants.ADMINS);
                    if(!admins.contains(preferenceManager.getString(Constants.USERNAME))){
                        showDialogChatButtonSettings.setVisibility(View.GONE);
                        if(arr.contains(preferenceManager.getString(Constants.USERNAME))){
                            showDialogChatButtonLeave.setVisibility(View.VISIBLE);
                            showDialogChatButtonJoin.setVisibility(View.GONE);
                        }else{
                            showDialogChatButtonLeave.setVisibility(View.GONE);
                            showDialogChatButtonJoin.setVisibility(View.VISIBLE);
                        }
                    }else{
                        showDialogChatButtonSettings.setVisibility(View.VISIBLE);
                        showDialogChatButtonLeave.setVisibility(View.GONE);
                        showDialogChatButtonJoin.setVisibility(View.GONE);
                    }
                });

        showDialogChatButtonSettings.setOnClickListener(view-> {
            dialog.dismiss();
            ShowLoading.dismissDialog();
            Intent intent = new Intent(this, ChannelSettingsActivity.class);
            intent.putExtra(Constants.COLLECTION_CHANNEL, receiverUser);
            startActivity(intent);
        });

        showDialogChatButtonLeave.setOnClickListener(view -> {
            ShowLoading.show(this);

            InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                    .whereEqualTo(Constants.USERNAME, binding.activityChatUsername.getText().toString())
                    .get()
                    .addOnSuccessListener(task -> {
                        ArrayList<String> arr = (ArrayList<String>) task.getDocuments().get(0).get(Constants.SUBSCRIBERS);
                        arr.remove(preferenceManager.getString(Constants.USERNAME));
                        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS).document(task.getDocuments().get(0).getId()).update(Constants.SUBSCRIBERS, arr);
                    });
            dialog.dismiss();
            ShowLoading.dismissDialog();
        });
        showDialogChatButtonJoin.setOnClickListener(view -> {
            ShowLoading.show(this);
            InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                    .whereEqualTo(Constants.USERNAME, binding.activityChatUsername.getText().toString())
                    .get()
                    .addOnSuccessListener(task -> {
                        ArrayList<String> arr = (ArrayList<String>) task.getDocuments().get(0).get(Constants.SUBSCRIBERS);
                        arr.add(preferenceManager.getString(Constants.USERNAME));
                        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS).document(task.getDocuments().get(0).getId()).update(Constants.SUBSCRIBERS, arr);
                    });
            dialog.dismiss();
            ShowLoading.dismissDialog();
        });

        dialog.setCancelable(true);
        dialog.create();
        dialog.show();
    }

    private void sendAttach(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickImage.launch(intent);
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
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void updateFirebase(){
        if (imageUri != null){
            ShowLoading.show(this);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_ATTACH + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        final String downloadUri = String.valueOf(uriTask.getResult());
                        HashMap<String, Object> message = new HashMap<>();
                        message.put(Constants.USERNAME, binding.activityChatUsername.getText().toString());
                        message.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_IMAGE);
                        message.put("image", downloadUri);
                        message.put(Constants.TIMESTAMP, new Date());
                        InitFirebase.firebaseFirestore.collection(Constants.CHANNELS_MSG).add(message);
                        if (conversationId != null){
                            updateConversion(binding.activityChatsMessage.getText().toString().trim());
                        } else {
                            HashMap<String, Object> conversation = new HashMap<>();
                            conversation.put(Constants.NAME, binding.activityChatUserName.getText().toString().trim());
                            conversation.put(Constants.USERNAME, binding.activityChatUsername.getText().toString().trim());
                            conversation.put(Constants.LAST_MESSAGE, "image");
                            conversation.put(Constants.IMAGE_PROFILE, downloadUri != null?downloadUri:Constants.DEFAULT_IMAGE_PROFILE);
                            conversation.put(Constants.TIMESTAMP, new Date());

                            ArrayList<String> subs2 = new ArrayList<>();
                            subs2.add(preferenceManager.getString(Constants.USERNAME));
                            conversation.put(Constants.SUBSCRIBERS, subs2);

                            addConversation(conversation);
                        }

                        binding.activityChatsMessage.setText(null);
                        ShowLoading.dismissDialog();
                    })
                    .addOnFailureListener(e -> {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(this, getResources().getString(R.string.error));
                    });
        }
    }


    private void sendMessage(){

        HashMap<String, Object> conversation = new HashMap<>();
        conversation.put(Constants.NAME, binding.activityChatUserName.getText().toString().trim());
        conversation.put(Constants.USERNAME, binding.activityChatUsername.getText().toString().trim());
        conversation.put(Constants.MESSAGE, binding.activityChatsMessage.getText().toString().trim());
        conversation.put(Constants.TIMESTAMP, new Date());
        conversation.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_TEXT);

        InitFirebase.firebaseFirestore.collection(Constants.CHANNELS_MSG).add(conversation);
        Log.d("ddd", "id: " + conversationId);
        if (conversationId != null){
            updateConversion(binding.activityChatsMessage.getText().toString().trim());
        } else {
            HashMap<String, Object> conversation2 = new HashMap<>();
            conversation2.put(Constants.NAME, binding.activityChatUserName.getText().toString().trim());
            conversation2.put(Constants.USERNAME, binding.activityChatUsername.getText().toString().trim());
            conversation2.put(Constants.LAST_MESSAGE, binding.activityChatsMessage.getText().toString().trim());
            conversation2.put(Constants.TIMESTAMP, new Date());

            ArrayList<String> subs2 = new ArrayList<>();
            subs2.add(preferenceManager.getString(Constants.USERNAME));
            conversation2.put(Constants.SUBSCRIBERS, subs2);

            addConversation(conversation2);
        }

        binding.activityChatsMessage.setText(null);
    }

    private void setWarningVisibility(Boolean isVisible){
        if (isVisible){
            binding.activityChatWarningLayout.setVisibility(View.VISIBLE);
        } else{
            binding.activityChatWarningLayout.setVisibility(View.GONE);
        }
    }
    private void listenMessages(){
        InitFirebase.firebaseFirestore.collection(Constants.CHANNELS_MSG)
                .whereEqualTo(Constants.USERNAME, binding.activityChatUsername.getText().toString())
                .addSnapshotListener(eventListener);
    }
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            Integer count = chatMessages.size();
            for (DocumentChange documentChange: value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    if(documentChange.getDocument().getString(Constants.MESSAGE_TYPE).equals(Constants.MESSAGE_TYPE_TEXT)){
                        chatMessage.senderId = binding.activityChatUsername.getText().toString();
                        chatMessage.messageType = Constants.MESSAGE_TYPE_TEXT;
                        chatMessage.message = documentChange.getDocument().getString(Constants.MESSAGE);
                        chatMessage.dateTime = getDateTime(documentChange.getDocument().getDate(Constants.TIMESTAMP));
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                    }else{
                        chatMessage.senderId = binding.activityChatUsername.getText().toString();
                        chatMessage.messageType = Constants.MESSAGE_TYPE_IMAGE;
                        chatMessage.image = documentChange.getDocument().getString(Constants.MESSAGE_TYPE_IMAGE);
                        chatMessage.dateTime = getDateTime(documentChange.getDocument().getDate(Constants.TIMESTAMP));
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                    }
                    chatMessages.add(chatMessage);
                }
            }
            if (chatMessages.isEmpty()){
                setWarningVisibility(true);
            } else {
                setWarningVisibility(false);
            }
            Collections.sort(chatMessages, (Object1, Object2) -> Object1.dateObject.compareTo(Object2.dateObject));
            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.activityChatsRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
        }
        if (conversationId == null){
            checkForConversation();
        }
    };
    private String getDateTime(Date date){
        return new SimpleDateFormat("dd MMMM, yyyy · hh:mm a", Locale.getDefault()).format(date);
    }
    private void checkForConversation(){
        if (chatMessages.size() != 0){
            checkForConversationRemotely(
                    binding.activityChatUsername.getText().toString()
            );
        }
    }
    private void checkForConversationRemotely(String senderId){
        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                .whereEqualTo(Constants.USERNAME, senderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        Log.d("doc", task.getResult().getDocuments().get(0).getId());
                        conversationId = documentSnapshot.getId();
                        Log.d("doc2", "id: " + conversationId);
                    }
                })
                .addOnFailureListener(e -> Log.d("error", e.toString()));
    }
    private void addConversation(HashMap<String, Object> conversation){
        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }
    private void updateConversion(String message){
        DocumentReference documentReference =
                InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS).document(conversationId);

        if(message.equals("")){
            documentReference.update(
                    Constants.LAST_MESSAGE,
                    Constants.MESSAGE_TYPE_IMAGE,
                    Constants.TIMESTAMP,
                    new Date()
            );
        }else{
            documentReference.update(
                    Constants.LAST_MESSAGE,
                    message,
                    Constants.TIMESTAMP,
                    new Date()
            );
        }
    }
}