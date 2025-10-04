package com.example.btlapp_prank.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.btlapp_prank.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private PrefManager prefManager;
    private DrawerLayout drawerLayout;
    private UserDB userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefManager = new PrefManager(this);
        userDB = new UserDB(this);   // khởi tạo database

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load fragment mặc định
        if (savedInstanceState == null) {
            loadFragment(new SoundFragment());
            bottomNavigationView.setSelectedItemId(R.id.action_sound);
        }

        // ================== Bottom Navigation (Fragment) ==================
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.action_home) {
                openHomeWithRoleCheck();
                return true;
            } else if (id == R.id.action_play) {
                openHomeWithRoleCheck1();
                return true;
            } else if (id == R.id.action_sound) {
                selectedFragment = new SoundFragment();
            } else if (id == R.id.action_setting) {
                openHomeWithRoleCheck2();
                return true;
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // ================== Navigation Drawer (Activity) ==================
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_exit) {
                logoutAndRedirect();
            } else if (id == R.id.action_home) {
                openHomeWithRoleCheck();
            } else if (id == R.id.action_setting) {
                openHomeWithRoleCheck2();
            } else if (id == R.id.action_about) {
                startActivity(new Intent(this, About.class));
            } else if (id == R.id.action_premium) {
                startActivity(new Intent(this, EnterCodeActivity.class));
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // ================= Kiểm tra role cho Home =================
    private void openHomeWithRoleCheck() {
        String role = prefManager.getCurrentUserRole();
        if ("admin".equalsIgnoreCase(role)) {
            loadFragment(new HomeFragment());
        } else {
            Toast.makeText(this, "Chỉ Admin mới được truy cập Home😊😊!", Toast.LENGTH_SHORT).show();
        }
    }
    private void openHomeWithRoleCheck1() {
        String role = prefManager.getCurrentUserRole();
        if ("admin".equalsIgnoreCase(role)) {
            loadFragment(new PlayFragment());
        } else {
            Toast.makeText(this, "Chỉ Admin mới được truy cập CHỦ ĐỀ 😒😒!", Toast.LENGTH_SHORT).show();
        }
    }
    private void openHomeWithRoleCheck2() {
        String role = prefManager.getCurrentUserRole();
        if ("admin".equalsIgnoreCase(role)) {
            loadFragment(new SettingsFragment());
        } else {
            Toast.makeText(this, "Chỉ Admin mới được truy cập SETTING😁😁!", Toast.LENGTH_SHORT).show();
        }
    }
    private void openHomeWithRoleCheck3() {
        String role = prefManager.getCurrentUserRole();
        if ("admin".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Toast.makeText(this, "Chỉ Admin mới được truy cập Home😊😊!", Toast.LENGTH_SHORT).show();
        }
    }
    private void openHomeWithRoleCheck4() {
        String role = prefManager.getCurrentUserRole();
        if ("admin".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, Settings.class));
        } else {
            Toast.makeText(this, "Chỉ Admin mới được truy cập SETTING😁😁!", Toast.LENGTH_SHORT).show();
        }
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
}
