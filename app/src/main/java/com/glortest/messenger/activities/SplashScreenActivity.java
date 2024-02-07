package com.glortest.messenger.activities;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.glortest.messenger.databinding.SplashScreenLayoutBinding;
import com.glortest.messenger.utilities.Constants;
import com.glortest.messenger.utilities.InitFirebase;
import com.glortest.messenger.utilities.PreferenceManager;
import com.glortest.messenger.utilities.Replace;

import java.util.Locale;


@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreenLayoutBinding binding = SplashScreenLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
        InitFirebase.init();
    }
    private void initFunc(){
        new Handler().postDelayed(() -> {
            if (preferenceManager.getBoolean(Constants.IS_SIGNED_IN)){
                Replace.replaceActivity(this, new MainActivity(), true);
            } else {
                Replace.replaceActivity(this, new RegistrationActivity(), true);
            }
        }, Constants.DELAY_MILLS);


        Configuration config = Resources.getSystem().getConfiguration();

        Locale locale = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            locale = config.getLocales().get(0);
        }
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, null);
    }
}