package com.glortest.messenger.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.glortest.messenger.databinding.RegistrationLayoutBinding;


public class RegistrationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RegistrationLayoutBinding binding = RegistrationLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}