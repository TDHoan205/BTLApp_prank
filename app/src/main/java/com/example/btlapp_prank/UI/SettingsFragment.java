package com.example.btlapp_prank.UI;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.btlapp_prank.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;

public class SettingsFragment extends Fragment {

    private SwitchMaterial switchSound, switchNotification, switchBiometric, switchDailyMeme, switchVibrate;
    private RadioGroup radioGroupTheme;
    private DrawerLayout drawerLayout;
    private MaterialButton btnChangePassword, btnLogout, btnFeedback;
    private MaterialTextView tvCurrentUser, tvVersion;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private Toast currentToast;
    private boolean isThemeChanging = false;

    private PrefManager prefManager;
    private UserDB userDB;

    public SettingsFragment() {
        // Constructor trống
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences("AppSettings", requireContext().MODE_PRIVATE);
        editor = prefs.edit();
        prefManager = new PrefManager(getContext());
        userDB = new UserDB(getContext());

        applySavedTheme();

        drawerLayout = view.findViewById(R.id.drawer_layout_settings);
        NavigationView navigationView = view.findViewById(R.id.nav_view_settings);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

//        // Toolbar quay lại
//        toolbar.setNavigationIcon(R.drawable.arrowback11);
//        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Ánh xạ view
        switchSound = view.findViewById(R.id.switch_sound);
        switchNotification = view.findViewById(R.id.switch_notification);
        switchBiometric = view.findViewById(R.id.switch_biometric);
        switchDailyMeme = view.findViewById(R.id.switch_daily_meme);
        switchVibrate = view.findViewById(R.id.switch_vibrate);

        radioGroupTheme = view.findViewById(R.id.radioGroup_theme);

        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnFeedback = view.findViewById(R.id.btn_feedback);

        tvCurrentUser = view.findViewById(R.id.tv_current_user);
        tvVersion = view.findViewById(R.id.tv_version);

        // Load dữ liệu lưu trước đó
        loadSettings();

        // Navigation Drawer xử lý
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                handleNavMenu(item);
                drawerLayout.closeDrawer(GravityCompat.END);
                return true;
            });
        }

        // Switch sự kiện
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("sound", isChecked).apply();
            showToast(isChecked ? "Âm thanh bật" : "Âm thanh tắt");
        });

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("notification", isChecked).apply();
            showToast(isChecked ? "Thông báo bật" : "Thông báo tắt");
        });

        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("biometric", isChecked).apply();
            showToast(isChecked ? "Bật đăng nhập sinh trắc học" : "Tắt đăng nhập sinh trắc học");
        });

        switchDailyMeme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("dailyMeme", isChecked).apply();
            showToast(isChecked ? "Meme hằng ngày bật" : "Meme hằng ngày tắt");
        });

        switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("vibrate", isChecked).apply();
            showToast(isChecked ? "Rung bật" : "Rung tắt");
        });

        // RadioGroup theme
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
            requireActivity().recreate();
        });

        // Nút đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Nút đăng xuất
        btnLogout.setOnClickListener(v -> logoutAndRedirect());

        // Nút feedback
        btnFeedback.setOnClickListener(v -> {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.setType("message/rfc822");
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@example.com"});
            email.putExtra(Intent.EXTRA_SUBJECT, "Góp ý ứng dụng Troll App");
            startActivity(Intent.createChooser(email, "Chọn ứng dụng email"));
        });

        // Set thông tin user
        String email = prefManager.getString("currentUserEmail");
        if (email != null) {
            tvCurrentUser.setText("📧 Đang đăng nhập: " + email);
        }

        // Phiên bản app
        tvVersion.setText("📱 Phiên bản: 1.0");
    }

    private void showChangePasswordDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        final EditText etOld = new EditText(requireContext());
        etOld.setHint("🔑 Mật khẩu cũ");
        etOld.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etOld);

        final EditText etNew = new EditText(requireContext());
        etNew.setHint("✨ Mật khẩu mới");
        etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNew);

        final EditText etConfirm = new EditText(requireContext());
        etConfirm.setHint("✅ Nhập lại mật khẩu mới");
        etConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirm);

        new MaterialAlertDialogBuilder(requireContext())
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

    private void handleNavMenu(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_home) {
            String role = prefManager.getString("currentUserRole");
            if ("admin".equalsIgnoreCase(role)) {
                startActivity(new Intent(getContext(), MainActivity1.class));
            } else {
                showToast("Chỉ Admin mới được truy cập Home!");
            }
        } else if (id == R.id.action_exit) {
            logoutAndRedirect();
        } else if (id == R.id.action_setting) {
            showToast("Bạn đã ở trang Cài đặt");
        } else if (id == R.id.action_about) {
            startActivity(new Intent(getContext(), About.class));
        } else if (id == R.id.action_premium) {
            startActivity(new Intent(getContext(), EnterCodeActivity.class));
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
        }
        startActivity(new Intent(getContext(), MainActivitylogin.class));
        requireActivity().finish();
    }

    private void applySavedTheme() {
        String theme = prefs.getString("theme", "system");
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
        switchSound.setChecked(prefs.getBoolean("sound", true));
        switchNotification.setChecked(prefs.getBoolean("notification", false));
        switchBiometric.setChecked(prefs.getBoolean("biometric", false));
        switchDailyMeme.setChecked(prefs.getBoolean("dailyMeme", false));
        switchVibrate.setChecked(prefs.getBoolean("vibrate", true));

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

    private void showToast(String message) {
        if (currentToast != null) currentToast.cancel();
        currentToast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
    }
}
