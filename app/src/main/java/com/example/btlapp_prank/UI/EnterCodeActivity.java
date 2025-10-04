package com.example.btlapp_prank.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.btlapp_prank.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

public class EnterCodeActivity extends AppCompatActivity {

    private LinearLayout btnYes, btnNo;
    private TextView tvStatus;
    private UserDB userDB;
    private PrefManager prefManager;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_code);

        // Ánh xạ view
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        tvStatus = findViewById(R.id.tvStatus);

        drawerLayout = findViewById(R.id.drawer_layout_premium);
        toolbar = findViewById(R.id.toolbar_premium);

        userDB = new UserDB(this);
        prefManager = new PrefManager(this);

        // Toolbar
        toolbar.setTitle("Kiểm tra Premium 😘😘");
//        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
//        toolbar.setNavigationOnClickListener(v -> logoutAndRedirect());

        // Navigation Drawer
        NavigationView navigationView = findViewById(R.id.nav_view_premium);
        navigationView.setNavigationItemSelectedListener(this::handleNavigation);

        // Bottom Navigation
//        bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setOnItemSelectedListener(this::handleBottomNavigation);

        // Xử lý nút Có / Không
        btnYes.setOnClickListener(v -> showPremiumDialog());
        btnNo.setOnClickListener(v -> {
            tvStatus.setText("Bạn đang dùng tài khoản thường");
            Toast.makeText(this, "Bạn đang dùng tài khoản thường", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
        });
    }

    /** ----------- HANDLE MENU BÊN (DRAWER) ----------- */
    private boolean handleNavigation(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_home) {
            openHomeWithRoleCheck();

        } else if (id == R.id.action_exit) {
            logoutAndRedirect();

        } else if (id == R.id.action_setting) {
            openHomeWithRoleCheckSetting();

        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, About.class));

        } else if (id == R.id.action_premium) {
            Toast.makeText(this, "Bạn đang ở trong trang Premium", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /** ----------- HANDLE MENU DƯỚI (BOTTOM) ----------- */
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
            startActivity(new Intent(this, Settings.class));
            return true;
        }
        return false;
    }

    /** ----------- CHECK ROLE CHO HOME ----------- */
    private void openHomeWithRoleCheck() {
        String role = prefManager.getCurrentUserRole();
        if ("admin".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Toast.makeText(this, "Chỉ Admin mới được truy cập Home!", Toast.LENGTH_SHORT).show();
        }
    }
    private void openHomeWithRoleCheckSetting() {
        String role = prefManager.getCurrentUserRole();
        if ("admin".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, Settings.class));
        } else {
            Toast.makeText(this, "Chỉ Admin mới được truy cập SETTING😁😁!", Toast.LENGTH_SHORT).show();
        }
    }

    /** ----------- LOGOUT ----------- */
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

    /** ----------- PREMIUM CODE ----------- */
    private void showPremiumDialog() {
        final EditText input = new EditText(this);
        input.setHint("🔑 Nhập mã Premium cực xịn...");
        input.setPadding(40, 30, 40, 30);
        input.setSingleLine(true);

        new MaterialAlertDialogBuilder(this)
                .setTitle("💎 Xác thực Premium 💎")
                .setMessage("Nhập mã VIP để mở khóa sức mạnh tối thượng (nhập sai thì thôi đấy 😜):")
                .setView(input)
                .setPositiveButton("🚀 Xác nhận", (dialog, which) -> {
                    String code = input.getText().toString().trim();
                    handleSubmitCode(code);
                })
                .setNegativeButton("❌ Thoát", (dialog, which) -> {
                    Toast.makeText(this, "😴 Hủy xác thực, đành làm dân thường vậy!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .show();
    }

    private void handleSubmitCode(String code) {
        String email = prefManager.getCurrentUserEmail();
        if (email == null || email.isEmpty()) {
            tvStatus.setText("Không tìm thấy user đang đăng nhập");
            return;
        }

        if (code.isEmpty()) {
            tvStatus.setText("Vui lòng nhập mã nâng cấp!");
            return;
        }

        if (code.equalsIgnoreCase("PREMIUM123")) {
            boolean updated = userDB.updateRole(email, "admin");
            if (updated) {
                tvStatus.setText("Nâng cấp thành công! Bạn đã trở thành admin.");
                Toast.makeText(this, "Nâng cấp thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
            } else {
                tvStatus.setText("Có lỗi khi cập nhật quyền. Thử lại!");
            }
        } else {
            tvStatus.setText("Mã không hợp lệ!");
        }
    }
}
