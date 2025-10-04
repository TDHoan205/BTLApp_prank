package com.example.btlapp_prank.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btlapp_prank.R;

public class SoundDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_ARTIST = "artist";
    public static final String EXTRA_IMAGE = "image";

    private String title;
    private String artist;
    private int imageRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset_sound_detail);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvArtist = findViewById(R.id.tvArtist);
        ImageView ivImage = findViewById(R.id.ivImage);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            title = intent.getStringExtra(EXTRA_TITLE);
            artist = intent.getStringExtra(EXTRA_ARTIST);
            imageRes = intent.getIntExtra(EXTRA_IMAGE, R.drawable.mj);
        }

        // Set dữ liệu ra view
        tvTitle.setText(title != null ? title : "Unknown Title");
        tvArtist.setText(artist != null ? artist : "Unknown Artist");
        ivImage.setImageResource(imageRes);
    }
}
