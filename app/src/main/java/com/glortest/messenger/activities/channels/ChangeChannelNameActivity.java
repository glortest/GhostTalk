package com.glortest.messenger.activities.channels;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

import com.glortest.messenger.R;
import com.glortest.messenger.activities.BaseActivity;
import com.glortest.messenger.databinding.ChangeChannelNameLayoutBinding;
import com.glortest.messenger.models.ChannelModel;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.ShowDialog;
import com.glortest.messenger.utilities.ShowLoading;
import com.glortest.messenger.utilities.ShowToast;

public class ChangeChannelNameActivity extends BaseActivity {
    private ChangeChannelNameLayoutBinding binding;
    private ChannelModel channel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        channel = (ChannelModel) getIntent().getSerializableExtra(Constants.COLLECTION_CHANNEL);
        super.onCreate(savedInstanceState);
        binding = ChangeChannelNameLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFunc();
    }
    private void initFunc(){
        setListeners();
        setUserInfo();
        binding.activityChangeBioNew.requestFocus();
        setMaxLength();
        setCount();
    }
    private void setUserInfo(){
        binding.activityChangeBioNew.setText(channel.name);
    }
    private void setListeners(){
        binding.activityChangeBioButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityChangeBioNew.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                setCount();
            }
        });
        binding.activityChangeBioButtonCheck.setOnClickListener(view -> {
            if (binding.activityChangeBioNew.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.name_can_not_be_empty));
            } else {
                changeBio();
            }
        });
    }
    private void changeBio(){
        if (!binding.activityChangeBioNew.getText().toString().trim().equals(channel.name)){
            ShowLoading.show(this);
            InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                    .whereEqualTo(Constants.USERNAME, channel.username)
                    .get()
                    .addOnSuccessListener(taskk ->{
                        InitFirebase.firebaseFirestore.collection(Constants.CHANNEL_CONVERSATIONS)
                                .document(taskk.getDocuments().get(0).getId())
                                .update(Constants.NAME, binding.activityChangeBioNew.getText().toString().trim())
                                .addOnSuccessListener(unused -> {
                                    ShowLoading.dismissDialog();
                                    ShowToast.show(this, getResources().getString(R.string.name_updated_successfully), false);
                                    onBackPressed();
                                }).addOnFailureListener(e -> {
                                    ShowLoading.dismissDialog();
                                    ShowDialog.show(this, getResources().getString(R.string.error));
                                });
                    });
        }
    }
    private void setMaxLength(){
        binding.activityChangeBioNew.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.NAME_MAX_LENGTH)});
    }
    @SuppressLint("SetTextI18n")
    private void setCount(){
        binding.activityChangeBioCount.setText(binding.activityChangeBioNew.getText().toString().trim().length() + getResources().getString(R.string.delimiter) + Constants.NAME_MAX_LENGTH);
    }
}