package com.example.btlapp_prank.UI;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.btlapp_prank.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class SoundFragment extends Fragment {

    private static final int ADD_SONG_REQUEST = 100;
    private static final int EDIT_SONG_REQUEST = 101;

    private GridView gridView;
    private FloatingActionButton fabMain, fabAdd, fabEdit, fabDelete, fabRefresh;
    private View fabMenu;
    private boolean isFabMenuOpen = false;

    private ArrayList<Sound> soundList;
    private SoundAdapter adapter;
    private PrefManager prefManager;
    private UserDB userDB;
    private SoundDB soundDB;
    private int selectedPos = -1;
    private String soundKey;

    private MediaPlayer mediaPlayer;
    private int currentlyPlaying = -1;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    public SoundFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sound, container, false);

        userDB = new UserDB(requireContext());
        prefManager = new PrefManager(requireContext());
        soundDB = new SoundDB(requireContext());

        drawerLayout = root.findViewById(R.id.drawer_layout);

        String buttonName = getActivity().getIntent().getStringExtra("button_name");
        soundKey = "sound_list_" + (buttonName != null ? buttonName : "default");

        Toolbar toolbar = root.findViewById(R.id.toolbar);
//        toolbar.setTitle(buttonName != null ? "Danh sách: " + buttonName : "Danh sách Sound");
//        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
//        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        navigationView = root.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this::handleNavigation);

        gridView = root.findViewById(R.id.gridView);

        fabMain = root.findViewById(R.id.fabMain);
        fabMenu = root.findViewById(R.id.fabMenu);
        fabAdd = root.findViewById(R.id.fabAdd);
        fabEdit = root.findViewById(R.id.fabEdit);
        fabDelete = root.findViewById(R.id.fabDelete);
        fabRefresh = root.findViewById(R.id.fabRefresh);

        soundList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();

        adapter = new SoundAdapter(requireContext(), soundList,
                (sound, position, btn) -> playSound(sound, position, btn));
        gridView.setAdapter(adapter);

        loadSongs();

        fabMain.setOnClickListener(v -> toggleFabMenu());

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddSoundActivity.class);
            intent.putExtra("mode", "add");
            startActivityForResult(intent, ADD_SONG_REQUEST);
            toggleFabMenu();
        });

        fabEdit.setOnClickListener(v -> {
            if (selectedPos != -1) {
                Sound sound = soundList.get(selectedPos);
                Intent intent = new Intent(requireContext(), AddSoundActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("pos", selectedPos);
                intent.putExtra("title", sound.getTitle());
                intent.putExtra("artist", sound.getArtist());
                if (sound.getImageUri() != null)
                    intent.putExtra("imageUri", sound.getImageUri().toString());
                if (sound.getAudioUri() != null)
                    intent.putExtra("audioUri", sound.getAudioUri().toString());
                startActivityForResult(intent, EDIT_SONG_REQUEST);
            } else {
                Toast.makeText(requireContext(), "Hãy chọn 1 mục để sửa", Toast.LENGTH_SHORT).show();
            }
            toggleFabMenu();
        });

        fabDelete.setOnClickListener(v -> {
            if (selectedPos != -1) {
                soundList.remove(selectedPos);
                prefManager.saveSoundsFor(soundKey, soundList);
                adapter.notifyDataSetChanged();
                selectedPos = -1;
            } else {
                Toast.makeText(requireContext(), "Hãy chọn 1 mục để xóa", Toast.LENGTH_SHORT).show();
            }
            toggleFabMenu();
        });

        fabRefresh.setOnClickListener(v -> {
            loadSongs();
            toggleFabMenu();
        });

        return root;
    }

    private void toggleFabMenu() {
        if (isFabMenuOpen) {
            fabMenu.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                fabMenu.setVisibility(View.GONE);
            }).start();
            isFabMenuOpen = false;
        } else {
            fabMenu.setAlpha(0f);
            fabMenu.setVisibility(View.VISIBLE);
            fabMenu.animate().alpha(1f).setDuration(200).start();
            isFabMenuOpen = true;
        }
    }

    private boolean handleNavigation(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            logoutAndRedirect();
        } else if (id == R.id.action_home) {
            openHomeWithRoleCheck();
        } else if (id == R.id.action_setting) {
            startActivity(new Intent(requireContext(), Settings.class));
        } else if (id == R.id.action_about) {
            startActivity(new Intent(requireContext(), About.class));
        } else if (id == R.id.action_premium) {
            startActivity(new Intent(requireContext(), EnterCodeActivity.class));
        } else if (id == R.id.action_play) {
            startActivity(new Intent(requireContext(), AddButtonActivity.class));
        } else if (id == R.id.action_sound) {
            // tự đang ở sound -> không làm gì
        }
        drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    private void openHomeWithRoleCheck() {
        String role = prefManager.getCurrentUserRole();
        if ("admin".equalsIgnoreCase(role)) {
            startActivity(new Intent(requireContext(), MainActivity1.class));
        } else {
            Toast.makeText(requireContext(), "Chỉ Admin mới được truy cập Home!", Toast.LENGTH_SHORT).show();
        }
    }

    private void playSound(Sound sound, int position, android.widget.ImageButton btn) {
        Uri audioUri = sound.getAudioUri();
        if (audioUri == null) {
            Toast.makeText(requireContext(), "File âm thanh không tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                    audioUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            if (currentlyPlaying != -1 && currentlyPlaying != position) {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.reset();
            }

            if (currentlyPlaying == position && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btn.setImageResource(R.drawable.play);
            } else {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(requireContext(), audioUri);
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    currentlyPlaying = position;
                    btn.setImageResource(R.drawable.pause);

                    sound.setPlayCount(sound.getPlayCount() + 1);
                    soundDB.updateSound(sound);
                    adapter.notifyDataSetChanged();
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    currentlyPlaying = -1;
                    btn.setImageResource(R.drawable.play);
                    adapter.notifyDataSetChanged();
                });
                mediaPlayer.prepareAsync();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Không mở được file âm thanh", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSongs() {
        soundList.clear();
        soundList.addAll(prefManager.getSoundsFor(soundKey));
        adapter.notifyDataSetChanged();
    }

    private void logoutAndRedirect() {
        prefManager.clearUserSession();
        gridView.postDelayed(() -> {
            startActivity(new Intent(requireContext(), MainActivitylogin.class));
            requireActivity().finish();
        }, 300);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // Dùng để nhận dữ liệu từ AddSoundActivity/EditSoundActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            boolean isUpdated = data.getBooleanExtra("isUpdated", false);
            if (!isUpdated) return;

            int id = data.getIntExtra("id", -1);
            String title = data.getStringExtra("title");
            String artist = data.getStringExtra("artist");
            Uri imageUri = data.hasExtra("imageUri") ? Uri.parse(data.getStringExtra("imageUri")) : null;
            Uri audioUri = data.hasExtra("audioUri") ? Uri.parse(data.getStringExtra("audioUri")) : null;

            Sound sound = new Sound(title, artist, imageUri, audioUri, prefManager.getCurrentUserEmail());
            sound.setId(id);

            if (requestCode == ADD_SONG_REQUEST) {
                soundList.add(sound);
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Đã thêm sound mới!", Toast.LENGTH_SHORT).show();
            } else if (requestCode == EDIT_SONG_REQUEST) {
                int pos = data.getIntExtra("pos", -1);
                if (pos != -1 && pos < soundList.size()) {
                    soundList.set(pos, sound);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "Đã cập nhật sound!", Toast.LENGTH_SHORT).show();
                }
            }
            prefManager.saveSoundsFor(soundKey, soundList);
        }
    }
}
