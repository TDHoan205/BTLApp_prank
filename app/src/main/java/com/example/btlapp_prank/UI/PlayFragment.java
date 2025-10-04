package com.example.btlapp_prank.UI;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.btlapp_prank.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayFragment extends Fragment {

    private GridLayout layoutContainer;
    private MaterialButton btnAddNew, btnDeleteSelected;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    private UserDB userDB;
    private PrefManager prefManager;
    private SoundDB soundDB;

    private boolean multiSelectMode = false;
    private Set<String> selectedButtons = new HashSet<>();

    private final int[] funColors = {
            Color.parseColor("#FF6F00"),
            Color.parseColor("#E040FB"),
            Color.parseColor("#00C853"),
            Color.parseColor("#FF1744"),
            Color.parseColor("#FF9100"),
            Color.parseColor("#2979FF"),
            Color.parseColor("#D500F9")
    };
    private int lastColorIndex = -1;

    public PlayFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_play, container, false);

        prefManager = new PrefManager(requireContext());
        userDB = new UserDB(requireContext());
        soundDB = new SoundDB(requireContext());

        drawerLayout = root.findViewById(R.id.drawer_layout);
        layoutContainer = root.findViewById(R.id.layoutContainer);
        btnAddNew = root.findViewById(R.id.btnAddNew);
        btnDeleteSelected = root.findViewById(R.id.btnDeleteSelected);
        toolbar = root.findViewById(R.id.toolbar);

//        toolbar.setTitle("Danh sách chủ đề âm thanh");
//        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
//        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        NavigationView navigationView = root.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this::handleNavigation);

        // Load từ DB
        List<String> categories = soundDB.getAllCategories();
        for (String name : categories) {
            createNewButton(name);
        }

        btnAddNew.setOnClickListener(v -> showAddButtonDialog());
        btnDeleteSelected.setOnClickListener(v -> {
            if (!selectedButtons.isEmpty()) {
                showDeleteConfirmDialog();
            } else {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setMessage("Bạn chưa chọn nút nào để xóa!")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

        return root;
    }

    private boolean handleNavigation(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            logoutAndRedirect();
        } else if (id == R.id.action_home) {
//            String role = prefManager.getCurrentUserRole();
//            if ("admin".equalsIgnoreCase(role)) {
//                startActivity(new Intent(requireContext(), MainActivity1.class));
//            } else {
//                Toast.makeText(requireContext(), "Chỉ Admin mới được truy cập Home!", Toast.LENGTH_SHORT).show();
//            }
            startActivity(new Intent(requireContext(), MainActivity.class));
        } else if (id == R.id.action_setting) {
            startActivity(new Intent(requireContext(), Settings.class));
        } else if (id == R.id.action_about) {
            startActivity(new Intent(requireContext(), About.class));
        } else if (id == R.id.action_premium) {
            startActivity(new Intent(requireContext(), EnterCodeActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    private void showAddButtonDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint("Nhập tên cho chủ đề bá đạo...");
        input.setPadding(40, 30, 40, 30);
        input.setSingleLine(true);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("😏 Tạo chủ đề troll mới 😏")
                .setMessage("Nhập tên cho chủ đề cực bá mà bạn muốn thêm:")
                .setIcon(R.drawable.a1)
                .setView(input)
                .setPositiveButton("🔥 Triệu hồi", (dialog, which) -> {
                    String buttonName = input.getText().toString().trim();
                    if (!buttonName.isEmpty()) {
                        long id = soundDB.addCategory(buttonName);
                        if (id != -1) {
                            createNewButton(buttonName);
                            Toast.makeText(requireContext(), "😆 Đã thêm \"" + buttonName + "\"!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "😱 Chủ đề \"" + buttonName + "\" đã tồn tại!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "🙃 Tên nút không được để trống!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("💤 Thôi nghỉ", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void createNewButton(String name) {
        MaterialButton newButton = new MaterialButton(requireContext());
        newButton.setText("🎵 " + name + " 🎵");
        newButton.setAllCaps(false);
        newButton.setTextColor(Color.WHITE);
        newButton.setTextSize(18);

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
                Toast.makeText(requireContext(), "👉 Bấm vào \"" + name + "\"!", Toast.LENGTH_SHORT).show();
                shakeButton(newButton);
                Intent intent = new Intent(requireContext(), MainActivityListView.class);
                intent.putExtra("button_name", name);
                startActivity(intent);
            }
        });

        newButton.setOnLongClickListener(v -> {
            multiSelectMode = true;
            toggleSelection(newButton, name);
            changeButtonColor(newButton);
            Toast.makeText(requireContext(), "😈 Đã chọn \"" + name + "\"!", Toast.LENGTH_SHORT).show();
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
                .translationXBy(10f).translationXBy(-10f)
                .setDuration(50)
                .withEndAction(() -> button.animate()
                        .translationXBy(10f).translationXBy(-10f)
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
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa " + selectedButtons.size() + " nút?")
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
                Toast.makeText(requireContext(), "Đã đăng xuất lúc: " + lastLogout, Toast.LENGTH_LONG).show();
            }
        }
        prefManager.clearUserSession();
        startActivity(new Intent(requireContext(), MainActivitylogin.class));
        requireActivity().finish();
    }
}
