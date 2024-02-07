package com.glortest.messenger.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.glortest.messenger.R;
import com.glortest.messenger.adapters.ChatAdapter;
import com.glortest.messenger.databinding.ChatLayoutBinding;
import com.glortest.messenger.models.ChatMessage;
import com.glortest.messenger.models.User;
import com.glortest.messenger.retrofit.Api;
import com.glortest.messenger.retrofit.ApiService;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;
import com.glortest.messenger.utilities.ShowLoading;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatActivity extends BaseActivity {
    private ChatLayoutBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private String conversationId;
    private Boolean isReceiverAvailable;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChatLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFields();
        initFunc();
    }
    private void initFields(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.USER);
        preferenceManager = new PreferenceManager(this);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                receiverUser.image,
                preferenceManager.getString(Constants.USER_ID),
                this
        );
        conversationId = null;
        isReceiverAvailable = false;
    }
    private void initFunc() {
        isBlocked();
        setListeners();
        setUserInfo();
        setAdapter();
        isBlocked();
        setSendButton();
        listenMessages();
    }
    private void setUserInfo(){
        binding.activityChatUserName.setText(receiverUser.name);
        Glide.with(this).load(receiverUser.image).into(binding.activityChatImageProfile);
    }
    @Override
    protected void onResume() {
        super.onResume();
        isBlocked();
        listenAvailable();
    }
    private void listenAvailable(){
        InitFirebase.firebaseFirestore.collection(Constants.USERS).document(
                receiverUser.id
        ).addSnapshotListener(this, ((value, error) -> {
            if (error != null){
                return;
            }
            if (value != null){
                if (value.getLong(Constants.AVAILABLE) != null){
                    int available = Objects.requireNonNull(
                            value.getLong(Constants.AVAILABLE)
                    ).intValue();
                    isReceiverAvailable = available == 1;
                }
                receiverUser.token = value.getString(Constants.FCM_TOKEN);
                if (receiverUser.image == null){
                    receiverUser.image = value.getString(Constants.IMAGE_PROFILE);
                    chatAdapter.setReceiverProfileImage(receiverUser.image);
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if (isReceiverAvailable){
                binding.activityChatOnline.setVisibility(View.VISIBLE);
            } else {
                binding.activityChatOnline.setVisibility(View.GONE);
            }
        }));
    }
    private void setAdapter(){
        binding.activityChatsRecyclerView.setAdapter(chatAdapter);
    }
    private void setSendButton(){
        if (binding.activityChatsMessage.getText().toString().trim().isEmpty()){
            binding.activityChatsButtonSend.setVisibility(View.INVISIBLE);
            binding.activityChatsButtonAttach.setVisibility(View.VISIBLE);
        }else {
            binding.activityChatsButtonSend.setVisibility(View.VISIBLE);
            binding.activityChatsButtonAttach.setVisibility(View.INVISIBLE);
        }
    }

    private void isBlocked(){
        InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_BLOCKED)
                .whereEqualTo(Constants.BLOCKED_ID, preferenceManager.getString(Constants.USER_ID))
                .whereEqualTo(Constants.BLOCKER_ID, receiverUser.id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()){
                                binding.blockedView.setVisibility(View.VISIBLE);
                                binding.sendView.setVisibility(View.INVISIBLE);
                            }else{
                                InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_BLOCKED)
                                        .whereEqualTo(Constants.BLOCKER_ID, preferenceManager.getString(Constants.USER_ID))
                                        .whereEqualTo(Constants.BLOCKED_ID, receiverUser.id)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if(!task.getResult().isEmpty()){
                                                        binding.blockedView.setVisibility(View.VISIBLE);
                                                        binding.sendView.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }


    private void setListeners(){
        binding.activityChatUser.setOnClickListener(view -> {
            Intent userActivity = new Intent(this, UserInfoActivity.class);
            userActivity.putExtra(Constants.USER, receiverUser);
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
        dialog.setContentView(R.layout.dialog_chat_action);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button showDialogChatDelete = dialog.findViewById(R.id.showDialogChatButtonDelete);
        Button showDialogChatArchive = dialog.findViewById(R.id.showDialogChatButtonArchive);
        Button showDialogChatFromArchive = dialog.findViewById(R.id.showDialogChatButtonFromArchive);

        InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
                .whereEqualTo(Constants.SENDER_ID, receiverUser.id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            if(task.getResult().getDocuments().get(0).getBoolean(Constants.IN_ARCHIVE)){
                                showDialogChatArchive.setVisibility(View.INVISIBLE);
                                showDialogChatFromArchive.setVisibility(View.VISIBLE);
                            }else{
                                showDialogChatArchive.setVisibility(View.VISIBLE);
                                showDialogChatFromArchive.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });

        InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                .whereEqualTo(Constants.RECEIVER_ID, receiverUser.id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            if(task.getResult().getDocuments().get(0).getBoolean(Constants.IN_ARCHIVE)){
                                showDialogChatArchive.setVisibility(View.INVISIBLE);
                                showDialogChatFromArchive.setVisibility(View.VISIBLE);
                            }else{
                                showDialogChatArchive.setVisibility(View.VISIBLE);
                                showDialogChatFromArchive.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });

        showDialogChatDelete.setOnClickListener(view -> {
            ShowLoading.show(this);
            InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_CHAT)
                    .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
                    .whereEqualTo(Constants.SENDER_ID, receiverUser.id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if(!task.getResult().isEmpty()) {
                                    for(int i = 0; i < task.getResult().size(); i++){
                                        InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_CHAT).document(task.getResult().getDocuments().get(i).getId()).delete();
                                    }
                                }
                            }
                        }
                    });

            InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_CHAT)
                    .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                    .whereEqualTo(Constants.RECEIVER_ID, receiverUser.id)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()) {
                                for(int i = 0; i < task.getResult().size(); i++){
                                    InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_CHAT).document(task.getResult().getDocuments().get(i).getId()).delete();
                                }
                            }
                        }
                    });
            InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                    .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
                    .whereEqualTo(Constants.SENDER_ID, receiverUser.id)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().delete();
                                }
                            }
                        }
                    });
            InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                    .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                    .whereEqualTo(Constants.RECEIVER_ID, receiverUser.id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if(!task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        document.getReference().delete();
                                    }
                                }
                            }
                        }
                    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            ShowLoading.dismissDialog();
                            //ShowToast.show(ChatActivity.this, "Chat was deleted", true);
                            Replace.replaceActivity(ChatActivity.this, new MainActivity(), false);
                        }
                    });

        });
        showDialogChatArchive.setOnClickListener(view -> {
            dialog.dismiss();

            InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                    .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
                    .whereEqualTo(Constants.SENDER_ID, receiverUser.id)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().update(Constants.IN_ARCHIVE, true);
                                }
                            }
                        }
                    });
            InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                    .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                    .whereEqualTo(Constants.RECEIVER_ID, receiverUser.id)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().update(Constants.IN_ARCHIVE, true);
                                }
                            }
                        }
                    }).addOnCompleteListener(task -> {
                        ShowLoading.dismissDialog();
                        //ShowToast.show(ChatActivity.this, "Chat is archived", true);
                        Replace.replaceActivity(ChatActivity.this, new MainActivity(), false);
                    });

        });

        showDialogChatFromArchive.setOnClickListener(view -> {
            dialog.dismiss();

            InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                    .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
                    .whereEqualTo(Constants.SENDER_ID, receiverUser.id)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().update(Constants.IN_ARCHIVE, false);
                                }
                            }
                        }
                    });
            InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                    .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                    .whereEqualTo(Constants.RECEIVER_ID, receiverUser.id)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().update(Constants.IN_ARCHIVE, false);
                                }
                            }
                        }
                    }).addOnCompleteListener(task -> {
                        ShowLoading.dismissDialog();
                        //ShowToast.show(ChatActivity.this, "Chat is unarchived", true);
                        Replace.replaceActivity(ChatActivity.this, new MainActivity(), false);
                    });

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
                        message.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
                        message.put(Constants.RECEIVER_ID, receiverUser.id);
                        message.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_IMAGE);
                        message.put("image", downloadUri);
                        message.put(Constants.TIMESTAMP, new Date());
                        InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_CHAT).add(message);
                        if (conversationId != null){
                            updateConversion(binding.activityChatsMessage.getText().toString().trim());
                        } else {
                            HashMap<String, Object> conversation = new HashMap<>();
                            conversation.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
                            conversation.put(Constants.SENDER_NAME, preferenceManager.getString(Constants.NAME));
                            conversation.put(Constants.SENDER_IMAGE, preferenceManager.getString(Constants.IMAGE_PROFILE));
                            conversation.put(Constants.RECEIVER_ID, receiverUser.id);
                            conversation.put(Constants.RECEIVER_NAME, receiverUser.name);
                            conversation.put(Constants.RECEIVER_IMAGE, receiverUser.image);
                            conversation.put(Constants.LAST_MESSAGE, "image");
                            conversation.put(Constants.TIMESTAMP, new Date());
                            conversation.put(Constants.IN_ARCHIVE, false);
                            addConversation(conversation);
                        }
                        if (!isReceiverAvailable){
                            try {
                                JSONArray tokens = new JSONArray();
                                tokens.put(receiverUser.token);
                                JSONObject data = new JSONObject();
                                data.put(Constants.USER_ID, preferenceManager.getString(Constants.USER_ID));
                                data.put(Constants.NAME, preferenceManager.getString(Constants.NAME));
                                data.put(Constants.FCM_TOKEN, preferenceManager.getString(Constants.FCM_TOKEN));
                                data.put(Constants.MESSAGE, "image");
                                JSONObject body = new JSONObject();
                                body.put(Constants.REMOTE_DATA, data);
                                body.put(Constants.REMOTE_REGISTRATION_IDS, tokens);
                                sendNotification(body.toString());
                            } catch (Exception e) {
                                //ShowToast.show(this, getResources().getString(R.string.error), true);
                            }
                        }
                        binding.activityChatsMessage.setText(null);
                        ShowLoading.dismissDialog();
                    })
                    .addOnFailureListener(e -> {
                        ShowLoading.dismissDialog();
                        //ShowDialog.show(this, getResources().getString(R.string.error));
                    });
        }
    }


    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
        message.put(Constants.RECEIVER_ID, receiverUser.id);
        message.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_TEXT);
        message.put(Constants.MESSAGE, binding.activityChatsMessage.getText().toString().trim());
        message.put(Constants.TIMESTAMP, new Date());
        InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_CHAT).add(message);
        if (conversationId != null){
            updateConversion(binding.activityChatsMessage.getText().toString().trim());
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
            conversation.put(Constants.SENDER_NAME, preferenceManager.getString(Constants.NAME));
            conversation.put(Constants.SENDER_IMAGE, preferenceManager.getString(Constants.IMAGE_PROFILE));
            conversation.put(Constants.RECEIVER_ID, receiverUser.id);
            conversation.put(Constants.RECEIVER_NAME, receiverUser.name);
            conversation.put(Constants.RECEIVER_IMAGE, receiverUser.image);
            conversation.put(Constants.LAST_MESSAGE, binding.activityChatsMessage.getText().toString().trim());
            conversation.put(Constants.TIMESTAMP, new Date());
            conversation.put(Constants.IN_ARCHIVE, false);
            addConversation(conversation);
        }
        if (!isReceiverAvailable){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);
                JSONObject data = new JSONObject();
                data.put(Constants.USER_ID, preferenceManager.getString(Constants.USER_ID));
                data.put(Constants.NAME, preferenceManager.getString(Constants.NAME));
                data.put(Constants.FCM_TOKEN, preferenceManager.getString(Constants.FCM_TOKEN));
                data.put(Constants.MESSAGE, binding.activityChatsMessage.getText().toString().trim());
                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_DATA, data);
                body.put(Constants.REMOTE_REGISTRATION_IDS, tokens);
                sendNotification(body.toString());
            } catch (Exception e) {
                //ShowToast.show(this, getResources().getString(R.string.error), true);
            }
        }
        binding.activityChatsMessage.setText(null);
    }
    private void sendNotification(String messageBody){
        Api.getClient().create(ApiService.class).sendMessages(
                Constants.getRemoteHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()){
                    try {
                        if (response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                //System.out.println(1111111111);
                                //ShowToast.show(ChatActivity.this, getResources().getString(R.string.error) + " " + error.getString("error"), true);
                            }
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                } else {
                    // ShowToast.show(ChatActivity.this, getResources().getString(R.string.error) + " " + response.code(), true);
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                // ShowToast.show(ChatActivity.this, getResources().getString(R.string.error), true);
            }
        });
    }
    private void setWarningVisibility(Boolean isVisible){
        if (isVisible){
            binding.activityChatWarningLayout.setVisibility(View.VISIBLE);
        } else{
            binding.activityChatWarningLayout.setVisibility(View.GONE);
        }
    }
    private void listenMessages(){
        InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_CHAT)
                .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                .whereEqualTo(Constants.RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_CHAT)
                .whereEqualTo(Constants.SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
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
                        chatMessage.senderId = documentChange.getDocument().getString(Constants.SENDER_ID);
                        chatMessage.receiverId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
                        chatMessage.messageType = Constants.MESSAGE_TYPE_TEXT;
                        chatMessage.message = documentChange.getDocument().getString(Constants.MESSAGE);
                        chatMessage.dateTime = getDateTime(documentChange.getDocument().getDate(Constants.TIMESTAMP));
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                    }else{
                        chatMessage.senderId = documentChange.getDocument().getString(Constants.SENDER_ID);
                        chatMessage.receiverId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Collections.sort(chatMessages, Comparator.comparing(object -> object.dateObject));
            }
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
                    preferenceManager.getString(Constants.USER_ID),
                    receiverUser.id
            );
            checkForConversationRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.USER_ID)
            );
        }
    }
    private void checkForConversationRemotely(String senderId, String receiverId){
        InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                .whereEqualTo(Constants.SENDER_ID, senderId)
                .whereEqualTo(Constants.RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        conversationId = documentSnapshot.getId();
                    }
                });
    }
    private void addConversation(HashMap<String, Object> conversation){
        InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }
    private void updateConversion(String message){
        DocumentReference documentReference =
                InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS).document(conversationId);

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