package com.example.btlapp_prank.UI;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.btlapp_prank.R;

public class HomeFragment extends Fragment {

    private static final int ADD_SOUND_REQUEST = 100;

    private SoundDB soundDB;
    private PrefManager prefManager;

    private LinearLayout btnCreate, btnSaved, btnImport, btnPre;

    // dùng ActivityResultLauncher thay cho onActivityResult cũ
    private ActivityResultLauncher<Intent> addSoundLauncher;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        soundDB = new SoundDB(requireContext());
        prefManager = new PrefManager(requireContext());

        btnCreate = root.findViewById(R.id.btnCreate);
        btnSaved = root.findViewById(R.id.btnSaved);
        btnImport = root.findViewById(R.id.btnImport);
        btnPre = root.findViewById(R.id.btnPre);

        // đăng ký launcher cho AddSoundActivity
        addSoundLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        handleAddSoundResult(result.getData());
                    }
                });

        setupButtons();

        return root;
    }

    private void setupButtons() {
        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddSoundActivity.class);
            intent.putExtra("mode", "add");
            addSoundLauncher.launch(intent);
        });

        btnSaved.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), MainActivityListView.class));
        });

        btnImport.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddButtonActivity.class));
        });

        btnPre.setOnClickListener(v -> {
            String email = prefManager.getCurrentUserEmail();
            Intent intent = new Intent(requireContext(), Statistics.class);
            intent.putExtra("email", email);
            startActivity(intent);
        });
    }

    private void handleAddSoundResult(Intent data) {
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

        Toast.makeText(requireContext(),
                result != -1 ? "Đã thêm: " + title + " - " + artist + " bởi " + createdBy
                        : "Lỗi khi thêm âm thanh",
                Toast.LENGTH_LONG).show();
    }
}
