package com.example.btlapp_prank.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.btlapp_prank.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity1 extends AppCompatActivity {

    private static final int ADD_SOUND_REQUEST = 100;

    private SoundDB soundDB;
    private UserDB userDB;
    private PrefManager prefManager;

    private Toolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        soundDB = new SoundDB(this);
        userDB = new UserDB(this);
        prefManager = new PrefManager(this);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("SoundBoard");
        toolbar.setNavigationIcon(R.drawable.arrowback11);
        toolbar.setNavigationOnClickListener(v -> logoutAndRedirect());

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_exit) {
                logoutAndRedirect();
            } else if (id == R.id.action_home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.action_setting) {
                startActivity(new Intent(this, SettingsFragment.class));
            } else if (id == R.id.action_about) {
                startActivity(new Intent(this, About.class));
            } else if (id == R.id.action_premium) {
                startActivity(new Intent(this, EnterCodeActivity.class));
            }
            return true;
        });
        // Bottom Navigation
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//            int id = item.getItemId();
//            if (id == R.id.action_home) {
//                // Mở màn hình chính
//                startActivity(new Intent(this, MainActivity.class));
//                return true;
//            } else if (id == R.id.action_play) {
//                // Mở màn hình Chủ đề
//                startActivity(new Intent(this, AddButtonActivity.class));
//                return true;
//            } else if (id == R.id.action_sound) {
//                // Mở danh sách Âm thanh
//                startActivity(new Intent(this, MainActivityListView.class));
//                return true;
//            } else if (id == R.id.action_setting) {
//                // Mở phần Cài đặt
//                startActivity(new Intent(this, Settings.class));
//                return true;
//            }
//            return false;
//        });

        LinearLayout btnCreate = findViewById(R.id.btnCreate);
        LinearLayout btnSaved = findViewById(R.id.btnSaved);
        LinearLayout btnImport = findViewById(R.id.btnImport);
        LinearLayout btnPre = findViewById(R.id.btnPre);

        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddSoundActivity.class);
            intent.putExtra("mode", "add");
            startActivityForResult(intent, ADD_SOUND_REQUEST);
        });

        btnSaved.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivityListView.class));
        });

        btnImport.setOnClickListener(v -> {
            startActivity(new Intent(this, AddButtonActivity.class));
        });

        btnPre.setOnClickListener(v -> {
            String email = prefManager.getCurrentUserEmail();
            Intent intent = new Intent(this, Statistics.class);
            intent.putExtra("email", email);
            startActivity(intent);
        });
    }

    private void logoutAndRedirect() {
        String email = prefManager.getCurrentUserEmail();
        if (email != null && !email.isEmpty()) {
            boolean logoutSuccess = userDB.updateLogoutTime(email);

            if (logoutSuccess) {
                String lastLogout = userDB.getLastLogout(email);
                Toast.makeText(this,
                        "Đã đăng xuất lúc: " + lastLogout,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật thời gian đăng xuất",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng",
                    Toast.LENGTH_SHORT).show();
        }

        prefManager.clearUserSession();
        startActivity(new Intent(this, MainActivitylogin.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_SOUND_REQUEST && resultCode == RESULT_OK && data != null) {
            String title = data.getStringExtra("title");
            String artist = data.getStringExtra("artist");
            String imageUri = data.getStringExtra("imageUri");
            String audioUri = data.getStringExtra("audioUri");

            String createdBy = prefManager.getCurrentUserEmail();

            Sound sound = new Sound(
                    title,
                    artist,
                    imageUri != null && !imageUri.isEmpty() ? Uri.parse(imageUri) : null,
                    audioUri != null && !audioUri.isEmpty() ? Uri.parse(audioUri) : null,
                    createdBy
            );

            long result = soundDB.addSound(sound);

            Toast.makeText(this,
                    result != -1 ? "Đã thêm: " + title + " - " + artist + " bởi " + createdBy
                            : "Lỗi khi thêm âm thanh",
                    Toast.LENGTH_LONG).show();
        }
    }
}
