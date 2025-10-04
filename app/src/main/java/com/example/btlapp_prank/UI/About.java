package com.example.btlapp_prank.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.btlapp_prank.R;
import com.google.android.material.navigation.NavigationView;

public class About extends AppCompatActivity {

    private PrefManager prefManager;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    private UserDB userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        prefManager = new PrefManager(this);

        drawerLayout = findViewById(R.id.drawer_layout_about);
        toolbar = findViewById(R.id.toolbar_about);

        // Toolbar setup
        toolbar.setTitle("Giới thiệu");
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        toolbar.setNavigationOnClickListener(v -> {
            // Quay về MainActivity
            finish();
        });

        // Navigation Drawer
        NavigationView navigationView = findViewById(R.id.nav_view_about);
        navigationView.setNavigationItemSelectedListener(item -> handleNavigation(item));
    }

    private boolean handleNavigation(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_home) {
            startActivity(new Intent(this, MainActivity.class));

        } else if (id == R.id.action_exit) {
           logoutAndRedirect();

        } else if (id == R.id.action_setting) {
            startActivity(new Intent(this, Settings.class));

        } else if (id == R.id.action_about) {
            Toast.makeText(this, "Bạn đang ở trang Giới thiệu😉😉", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.action_premium) {
            startActivity(new Intent(this, EnterCodeActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
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
}
