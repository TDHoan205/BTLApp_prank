package com.example.btlapp_prank.UI;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.btlapp_prank.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddButtonActivity extends AppCompatActivity {

    private GridLayout layoutContainer;
    private MaterialButton btnAddNew, btnDeleteSelected;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    private UserDB userDB;
    private PrefManager prefManager;
    private SoundDB soundDB;
    private BottomNavigationView bottomNavigationView;

    private boolean multiSelectMode = false;
    private Set<String> selectedButtons = new HashSet<>();

    // Danh sách màu troll để random
    private final int[] funColors = {
            Color.parseColor("#FF6F00"), // Cam đậm
            Color.parseColor("#E040FB"), // Tím hồng
            Color.parseColor("#00C853"), // Xanh lá neon
            Color.parseColor("#FF1744"), // Đỏ chói
            Color.parseColor("#FF9100"), // Cam sáng
            Color.parseColor("#2979FF"), // Xanh dương chói
            Color.parseColor("#D500F9")  // Tím nhức mắt
    };
    private int lastColorIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset_add_button);

        prefManager = new PrefManager(this);
        userDB = new UserDB(this);
        soundDB = new SoundDB(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        layoutContainer = findViewById(R.id.layoutContainer);
        btnAddNew = findViewById(R.id.btnAddNew);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        toolbar = findViewById(R.id.toolbar);

        // Toolbar setup
        toolbar.setTitle("Danh sách chủ đề âm thanh");
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Navigation Drawer
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this::handleNavigation);



        // Load categories từ DB
        List<String> categories = soundDB.getAllCategories();
        for (String name : categories) {
            createNewButton(name);
        }

        btnAddNew.setOnClickListener(v -> showAddButtonDialog());
        btnDeleteSelected.setOnClickListener(v -> {
            if (!selectedButtons.isEmpty()) {
                showDeleteConfirmDialog();
            } else {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setMessage("Bạn chưa chọn nút nào để xóa!")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private boolean handleNavigation(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            logoutAndRedirect();
        } else if (id == R.id.action_home) {
//            String role = prefManager.getCurrentUserRole();
//            if ("admin".equalsIgnoreCase(role)) {
//                startActivity(new Intent(this, MainActivity1.class));
//            } else {
//                Toast.makeText(this, "Chỉ Admin mới được truy cập Home!", Toast.LENGTH_SHORT).show();
//            }
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

    private void showAddButtonDialog() {
        final EditText input = new EditText(this);
        input.setHint("Nhập tên cho chủ đề bá đạo...");
        input.setPadding(40, 30, 40, 30);
        input.setSingleLine(true);

        new MaterialAlertDialogBuilder(this)
                .setTitle("😏 Tạo chủ đề troll mới 😏")
                .setMessage("Nhập tên cho chủ đề cực bá mà bạn muốn thêm (đừng trùng kẻo quê nha 😎):")
                .setIcon(R.drawable.a1)
                .setView(input)
                .setPositiveButton("🔥 Triệu hồi", (dialog, which) -> {
                    String buttonName = input.getText().toString().trim();
                    if (!buttonName.isEmpty()) {
                        long id = soundDB.addCategory(buttonName);
                        if (id != -1) {
                            createNewButton(buttonName);
                            Toast.makeText(this, "😆 Đã triệu hồi nút \"" + buttonName + "\" thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "😱 Chủ đề \"" + buttonName + "\" đã tồn tại rồi đó, bớt troll đi!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "🙃 Tên nút không được để trống, nhập gì đó cho có khí thế chứ!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("💤 Thôi nghỉ", (dialog, which) -> {
                    Toast.makeText(this, "😴 Hủy rồi, lười nhỉ!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .show();
    }

    private void createNewButton(String name) {
        MaterialButton newButton = new MaterialButton(this);
        newButton.setText("🎵 " + name + " 🎵");
        newButton.setAllCaps(false);
        newButton.setTextColor(Color.WHITE);
        newButton.setTextSize(18);

        // Random màu, tránh trùng
        int colorIndex;
        do {
            colorIndex = (int) (Math.random() * funColors.length);
        } while (colorIndex == lastColorIndex);
        lastColorIndex = colorIndex;

        int color = funColors[colorIndex];
        newButton.setBackgroundTintList(null);

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{color, lightenColor(color, 0.3f)}
        );
        bg.setCornerRadius(64);
        newButton.setBackground(bg);

        newButton.setRippleColor(ColorStateList.valueOf(lightenColor(color, 0.4f)));
        newButton.setElevation(12f);

        int padding = (int) (12 * getResources().getDisplayMetrics().density);
        newButton.setPadding(padding, padding, padding, padding);

        // Mỗi hàng chỉ có 1 nút → full width
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = GridLayout.LayoutParams.MATCH_PARENT;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(0, 1f);
        params.setMargins(padding, padding, padding, padding);
        newButton.setLayoutParams(params);

        newButton.setOnClickListener(v -> {
            if (multiSelectMode) {
                toggleSelection(newButton, name);
                if (selectedButtons.isEmpty()) multiSelectMode = false;
            } else {
                Toast.makeText(this, "👉 Bạn vừa bấm vào \"" + name + "\" rồi đó nha 😎", Toast.LENGTH_SHORT).show();
                shakeButton(newButton); // rung nút
                Intent intent = new Intent(this, MainActivityListView.class);
                intent.putExtra("button_name", name);
                startActivity(intent);
            }
        });

        // Long click → đổi màu troll
        newButton.setOnLongClickListener(v -> {
            multiSelectMode = true;
            toggleSelection(newButton, name);
            changeButtonColor(newButton);
            Toast.makeText(this, "😈 Chủ đề \"" + name + "\" đã bị chọn và đổi màu!", Toast.LENGTH_SHORT).show();
            return true;
        });

        layoutContainer.addView(newButton);
    }

    private int lightenColor(int color, float factor) {
        int r = Math.min(255, (int) (Color.red(color) * (1 + factor)));
        int g = Math.min(255, (int) (Color.green(color) * (1 + factor)));
        int b = Math.min(255, (int) (Color.blue(color) * (1 + factor)));
        return Color.rgb(r, g, b);
    }

    private void toggleSelection(MaterialButton btn, String name) {
        if (selectedButtons.contains(name)) {
            selectedButtons.remove(name);
            btn.setAlpha(1f);
        } else {
            selectedButtons.add(name);
            btn.setAlpha(0.6f);
        }
    }

    private void shakeButton(MaterialButton button) {
        button.animate()
                .translationXBy(10f)
                .translationXBy(-10f)
                .setDuration(50)
                .withEndAction(() -> button.animate()
                        .translationXBy(10f)
                        .translationXBy(-10f)
                        .setDuration(50))
                .start();
    }

    private void changeButtonColor(MaterialButton button) {
        int colorIndex = (int) (Math.random() * funColors.length);
        int newColor = funColors[colorIndex];
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{newColor, lightenColor(newColor, 0.3f)}
        );
        bg.setCornerRadius(64);
        button.setBackground(bg);
    }

    private void showDeleteConfirmDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa " + selectedButtons.size() + " nút đã chọn không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteSelected())
                .setNegativeButton("Hủy", (dialog, which) -> {
                    clearSelections();
                    dialog.dismiss();
                })
                .show();
    }

    private void deleteSelected() {
        for (String name : selectedButtons) {
            soundDB.deleteCategory(name);
        }

        for (int i = 0; i < layoutContainer.getChildCount(); i++) {
            if (layoutContainer.getChildAt(i) instanceof MaterialButton) {
                MaterialButton btn = (MaterialButton) layoutContainer.getChildAt(i);
                if (selectedButtons.contains(btn.getText().toString())) {
                    layoutContainer.removeView(btn);
                    i--;
                }
            }
        }
        selectedButtons.clear();
        multiSelectMode = false;
    }

    private void clearSelections() {
        for (int i = 0; i < layoutContainer.getChildCount(); i++) {
            if (layoutContainer.getChildAt(i) instanceof MaterialButton) {
                MaterialButton btn = (MaterialButton) layoutContainer.getChildAt(i);
                btn.setAlpha(1f);
            }
        }
        selectedButtons.clear();
        multiSelectMode = false;
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
