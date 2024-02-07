package com.glortest.messenger.activities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.glortest.messenger.adapters.UsersAdapter;
import com.glortest.messenger.databinding.ContactsLayoutBinding;
import com.glortest.messenger.listeners.UserListeners;
import com.glortest.messenger.models.User;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.ShowLoading;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class ContactsActivity extends BaseActivity implements UserListeners {
    private ContactsLayoutBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ContactsLayoutBinding.inflate(getLayoutInflater());
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
        binding.findChatActivityButtonBack.setOnClickListener(view -> onBackPressed());
        getContactList();
        findFriend();
    }

    ArrayList<String> contactList = new ArrayList<>();

    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private void getContactList() {
        ContentResolver cr = getContentResolver();

        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor != null) {
            HashSet<String> mobileNoSet = new HashSet<String>();
            try {
                final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String number;
                while (cursor.moveToNext()) {
                    number = cursor.getString(numberIndex);
                    number = number.replace(" ", "");
                    if (!mobileNoSet.contains(number)) {
                        contactList.add(number);
                        mobileNoSet.add(number);
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void findFriend(){
        ShowLoading.show(this);
        users.clear();
        for(String number: contactList){
            InitFirebase.firebaseFirestore.collection(Constants.USERS)
                    .whereEqualTo(Constants.PHONE, number)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                            for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                                if (preferenceManager.getString(Constants.USER_ID).equals(queryDocumentSnapshot.getId())){
                                    ShowLoading.dismissDialog();
                                } else {
                                    User user = new User();
                                    user.name = queryDocumentSnapshot.getString(Constants.NAME);
                                    user.phone = queryDocumentSnapshot.getString(Constants.PHONE);
                                    user.image = queryDocumentSnapshot.getString(Constants.IMAGE_PROFILE);
                                    user.token = queryDocumentSnapshot.getString(Constants.FCM_TOKEN);
                                    user.id = queryDocumentSnapshot.getId();
                                    users.add(user);
                                    ShowLoading.dismissDialog();
                                }
                            }
                            if (users.size() > 0){
                                UsersAdapter usersAdapter = new UsersAdapter(users, this, this);
                                binding.contactsActivityRecyclerView.setAdapter(usersAdapter);
                            }
                        }
                    }).addOnFailureListener(e -> {
                        ShowLoading.dismissDialog();
                    });
        }

    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.USER, user);
        startActivity(intent);
        finish();
    }
}