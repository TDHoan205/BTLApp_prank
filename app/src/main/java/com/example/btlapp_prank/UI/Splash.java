package com.example.btlapp_prank.UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btlapp_prank.R;

public class Splash extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Chuyển qua MainActivitylogin sau vài giây
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(Splash.this, MainActivitylogin.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
