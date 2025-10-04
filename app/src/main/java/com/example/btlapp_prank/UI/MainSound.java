package com.example.btlapp_prank.UI;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.btlapp_prank.R;

public class MainSound extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("🎵 Main Sound");
        toolbar.setBackgroundColor(getResources().getColor(android.R.color.white));

        // Set toolbar làm ActionBar
        setSupportActionBar(toolbar);
    }
}
