package com.example.btlapp_prank.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.btlapp_prank.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class Settings extends AppCompatActivity {

    private SwitchMaterial switchSound, switchNotification;
    private RadioGroup radioGroupTheme;
    private DrawerLayout drawerLayout;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private Toast currentToast;
    private boolean isThemeChanging = false;

    private PrefManager prefManager;
    private UserDB userDB; // DB để lưu thông tin đăng xuất & mật khẩu

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        editor = prefs.edit();
        prefManager = new PrefManager(this);
        userDB = new UserDB(this);

        applySavedTheme();

        drawerLayout = findViewById(R.id.drawer_layout_settings);
        NavigationView navigationView = findViewById(R.id.nav_view_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.arrowback11);
        toolbar.setNavigationOnClickListener(v -> logoutAndRedirect());

        switchSound = findViewById(R.id.switch_sound);
        switchNotification = findViewById(R.id.switch_notification);
        radioGroupTheme = findViewById(R.id.radioGroup_theme);

        loadSettings();

        // Xử lý menu thanh bên
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_home) {
//                    String role = prefManager.getString("currentUserRole");
//                    if ("admin".equalsIgnoreCase(role)) {
//                        startActivity(new Intent(this, MainActivity.class));
//                    } else {
//                        showToast("Chỉ Admin mới được truy cập Home!");
//                    }
                    startActivity(new Intent(this, MainActivity.class));
                } else if (id == R.id.action_exit) {
                    logoutAndRedirect();

                } else if (id == R.id.action_setting) {
                    showToast("Bạn đã ở trang Cài đặt");

                } else if (id == R.id.action_about) {
                    startActivity(new Intent(this, About.class));

                } else if (id == R.id.action_premium) {
                    startActivity(new Intent(this, EnterCodeActivity.class));
                }
                // ---- Đổi mật khẩu ----

                drawerLayout.closeDrawer(GravityCompat.END);
                return true;
            });
        }

        if (toolbar != null) {
            toolbar.setTitle("Cài đặt");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (switchSound != null) {
            switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                editor.putBoolean("sound", isChecked).apply();
                showToast(isChecked ? "Âm thanh bật" : "Âm thanh tắt");
            });
        }

        if (switchNotification != null) {
            switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
                editor.putBoolean("notification", isChecked).apply();
                showToast(isChecked ? "Thông báo bật" : "Thông báo tắt");
            });
        }

        if (radioGroupTheme != null) {
            radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
                if (isThemeChanging) return;

                isThemeChanging = true;
                if (checkedId == R.id.rb_light) {
                    editor.putString("theme", "light").apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else if (checkedId == R.id.rb_dark) {
                    editor.putString("theme", "dark").apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else if (checkedId == R.id.rb_system) {
                    editor.putString("theme", "system").apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }
                recreate();
            });
        }
    }

    // ---- Dialog đổi mật khẩu ----
    private void showChangePasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        final EditText etOld = new EditText(this);
        etOld.setHint("🔑 Mật khẩu cũ");
        etOld.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etOld);

        final EditText etNew = new EditText(this);
        etNew.setHint("✨ Mật khẩu mới");
        etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNew);

        final EditText etConfirm = new EditText(this);
        etConfirm.setHint("✅ Nhập lại mật khẩu mới");
        etConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirm);

        new MaterialAlertDialogBuilder(this)
                .setTitle("😎 Đổi mật khẩu")
                .setMessage("Nhập mật khẩu cũ và đặt mật khẩu mới nhé")

                .setView(layout)
                .setPositiveButton("🚀 Đổi ngay", (dialog, which) -> {
                    String oldPass = etOld.getText().toString().trim();
                    String newPass = etNew.getText().toString().trim();
                    String confirmPass = etConfirm.getText().toString().trim();

                    if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                        showToast("⚠️ Vui lòng nhập đầy đủ thông tin!");
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        showToast("❌ Mật khẩu nhập lại không khớp!");
                        return;
                    }

                    String email = prefManager.getString("currentUserEmail");
                    if (email != null && userDB.checkPassword(email, oldPass)) {
                        boolean updated = userDB.updatePassword(email, newPass);
                        if (updated) {
                            showToast("✅ Đổi mật khẩu thành công!");
                        } else {
                            showToast("⚠️ Lỗi khi cập nhật mật khẩu!");
                        }
                    } else {
                        showToast("❌ Mật khẩu cũ không đúng!");
                    }
                })
                .setNegativeButton("💤 Hủy", (dialog, which) -> {
                    dialog.dismiss();
                    showToast("😴 Thôi khỏi đổi!");
                })
                .show();
    }

    // ================== CÁC HÀM KHÁC ==================

    private boolean handleBottomNavigation(@NonNull android.view.MenuItem item) {
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
            showToast("Bạn đã ở trang Cài đặt");
            return true;
        }
        return false;
    }

    private void openHomeWithRoleCheck() {
        String role = prefManager.getString("currentUserRole");
        if ("admin".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, MainActivity1.class));
        } else {
            showToast("Chỉ Admin mới được truy cập Home!");
        }
    }

    private void logoutAndRedirect() {
        String email = prefManager.getString("currentUserEmail");
        if (email != null && !email.isEmpty()) {
            boolean logoutSuccess = userDB.updateLogoutTime(email);

            if (logoutSuccess) {
                String lastLogout = userDB.getLastLogout(email);
                showToast("Đăng xuất lúc: " + lastLogout);
            } else {
                showToast("Lỗi khi cập nhật thời gian đăng xuất");
            }

            prefManager.clearAll();
        } else {
            showToast("Không tìm thấy thông tin người dùng");
        }

        startActivity(new Intent(this, MainActivitylogin.class));
        finish();
    }

    private void applySavedTheme() {
        String theme = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getString("theme", "system");
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private void loadSettings() {
        if (switchSound != null) {
            switchSound.setChecked(prefs.getBoolean("sound", true));
        }
        if (switchNotification != null) {
            switchNotification.setChecked(prefs.getBoolean("notification", false));
        }
        if (radioGroupTheme != null) {
            String theme = prefs.getString("theme", "system");
            isThemeChanging = true;
            if ("light".equals(theme)) {
                radioGroupTheme.check(R.id.rb_light);
            } else if ("dark".equals(theme)) {
                radioGroupTheme.check(R.id.rb_dark);
            } else {
                radioGroupTheme.check(R.id.rb_system);
            }
            isThemeChanging = false;
        }
    }

    private void showToast(String message) {
        if (currentToast != null) currentToast.cancel();
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
    }
}
