package com.example.btlapp_prank.UI;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.btlapp_prank.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

public class AddSoundActivity extends AppCompatActivity {

    private EditText edtTitle, edtArtist;
    private MaterialButton btnSelectImage, btnSelectAudio, btnSave, btnCancel;
    private Uri imageUri, audioUri;
    private String mode;
    private int pos = -1;
    private int editId = -1;

    private static final int PICK_IMAGE = 200;
    private static final int PICK_AUDIO = 201;

    private PrefManager prefManager;
    private UserDB userDB;
    private SoundDB soundDB;
    private String categoryName;

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset_add_sound);

        prefManager = new PrefManager(this);
        userDB = new UserDB(this);
        soundDB = new SoundDB(this);

        edtTitle = findViewById(R.id.edtTitle);
        edtArtist = findViewById(R.id.edtCreator);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSelectAudio = findViewById(R.id.btnSelectAudio);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Thêm/Sửa Sound");
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Menu bên (Navigation Drawer)
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this::handleNavigation);

        // Menu dưới (Bottom Navigation)
//        bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setOnItemSelectedListener(this::handleBottomNavigation);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            mode = intent.getStringExtra("mode");
            categoryName = intent.getStringExtra("button_name");

            if ("edit".equals(mode)) {
                pos = intent.getIntExtra("pos", -1);
                editId = intent.getIntExtra("id", -1);
                edtTitle.setText(intent.getStringExtra("title"));
                edtArtist.setText(intent.getStringExtra("artist"));

                String img = intent.getStringExtra("imageUri");
                if (img != null) imageUri = Uri.parse(img);
                String audio = intent.getStringExtra("audioUri");
                if (audio != null) audioUri = Uri.parse(audio);
            }
        }

        btnSelectImage.setOnClickListener(v -> {
            Intent pickImg = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pickImg.setType("image/*");
            startActivityForResult(pickImg, PICK_IMAGE);
        });

        btnSelectAudio.setOnClickListener(v -> {
            Intent pickAudio = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pickAudio.setType("audio/*");
            pickAudio.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(pickAudio, PICK_AUDIO);
        });

        btnSave.setOnClickListener(v -> saveSound());
        btnCancel.setOnClickListener(v -> finish());
    }

    /** ----------- HANDLE MENU BÊN (DRAWER) ----------- */
    private boolean handleNavigation(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            logoutAndRedirect();
        } else if (id == R.id.action_home) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.action_setting) {
            startActivity(new Intent(this, Settings.class));
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, About.class));
        } else if (id == R.id.action_premium) {
            startActivity(new Intent(this, EnterCodeActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    /** ----------- HANDLE MENU DƯỚI (BOTTOM) ----------- */
    private boolean handleBottomNavigation(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_home) {
            openHomeWithRoleCheck();
            return true;
        } else if (id == R.id.action_play) {
            startActivity(new Intent(this, AddButtonActivity.class));
            return true;
        } else if (id == R.id.action_sound) {
            startActivity(new Intent(this, MainActivityListView.class));
            return true;
        } else if (id == R.id.action_setting) {
            startActivity(new Intent(this, Settings.class));
            return true;
        }
        return false;
    }

    /** ----------- CHECK ROLE CHO HOME ----------- */
    private void openHomeWithRoleCheck() {
        String role = prefManager.getCurrentUserRole();
        if ("admin".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, MainActivity1.class));
        } else {
            Toast.makeText(this, "Chỉ Admin mới được truy cập Home!", Toast.LENGTH_SHORT).show();
        }
    }

    /** ----------- LOGOUT ----------- */
    private void logoutAndRedirect() {
        String email = prefManager.getCurrentUserEmail();
        if (email != null && !email.isEmpty()) {
            boolean logoutSuccess = userDB.updateLogoutTime(email);
            if (logoutSuccess) {
                String lastLogout = userDB.getLastLogout(email);
                Toast.makeText(this, "Đã đăng xuất lúc: " + lastLogout, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật thời gian đăng xuất", Toast.LENGTH_SHORT).show();
            }
        }
        prefManager.clearUserSession();
        startActivity(new Intent(this, MainActivitylogin.class));
        finish();
    }

    /** ----------- SAVE SOUND ----------- */
    private void saveSound() {
        String title = edtTitle.getText().toString().trim();
        String artist = edtArtist.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên Sound", Toast.LENGTH_SHORT).show();
            return;
        }

        Sound sound = new Sound(title, artist, imageUri, audioUri, prefManager.getCurrentUserEmail());

        Intent resultIntent = new Intent();
        resultIntent.putExtra("title", title);
        resultIntent.putExtra("artist", artist);
        if (imageUri != null) resultIntent.putExtra("imageUri", imageUri.toString());
        if (audioUri != null) resultIntent.putExtra("audioUri", audioUri.toString());
        resultIntent.putExtra("isUpdated", true);

        if ("add".equals(mode)) {
            long id = soundDB.addSound(sound, categoryName);
            if (id != -1) {
                editId = (int) id;
                sound.setId(editId);
            }
            resultIntent.putExtra("id", sound.getId());
            setResult(Activity.RESULT_OK, resultIntent);
        } else if ("edit".equals(mode) && editId != -1) {
            sound.setId(editId);
            soundDB.updateSound(sound);
            resultIntent.putExtra("id", sound.getId());
            resultIntent.putExtra("pos", pos);
            setResult(Activity.RESULT_OK, resultIntent);
        }

        finish();
    }

    /** ----------- HANDLE KẾT QUẢ CHỌN ẢNH/AUDIO ----------- */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) return;

        Uri selected = data.getData();
        if (requestCode == PICK_IMAGE) {
            imageUri = selected;
            Toast.makeText(this, "Đã chọn ảnh", Toast.LENGTH_SHORT).show();
        } else if (requestCode == PICK_AUDIO) {
            audioUri = selected;
            try {
                getContentResolver().takePersistableUriPermission(
                        selected, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể cấp quyền cho file âm thanh", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, "Đã chọn file âm thanh", Toast.LENGTH_SHORT).show();
        }
    }
}
