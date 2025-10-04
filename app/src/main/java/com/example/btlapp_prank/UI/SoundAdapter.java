package com.example.btlapp_prank.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.btlapp_prank.R;

import java.util.ArrayList;

public class SoundAdapter extends BaseAdapter {

    public interface OnPlayClickListener {
        void onPlayClick(Sound sound, int position, ImageButton btn);
    }

    private final Context context;
    private final ArrayList<Sound> soundList;
    private final OnPlayClickListener listener;

    // 🔹 Vị trí bài hát đang phát
    private int currentlyPlaying = -1;

    public SoundAdapter(Context context, ArrayList<Sound> soundList, OnPlayClickListener listener) {
        this.context = context;
        this.soundList = soundList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return soundList.size();
    }

    @Override
    public Object getItem(int position) {
        return soundList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setCurrentlyPlaying(int pos) {
        this.currentlyPlaying = pos;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.item_preset_sound, parent, false);

        ImageView ivImage = convertView.findViewById(R.id.ivSong);
        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvArtist = convertView.findViewById(R.id.tvArtist);
        TextView tvPlayCount = convertView.findViewById(R.id.tvPlayCount);
        ImageButton btnPlayPause = convertView.findViewById(R.id.btnPlayPause);

        Sound sound = soundList.get(position);

        // Ảnh đại diện
        if (sound.getImageUri() != null) {
            Glide.with(context).load(sound.getImageUri())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(ivImage);
        } else {
            ivImage.setImageResource(R.mipmap.ic_launcher);
        }

        // Thông tin
        tvTitle.setText(sound.getTitle());
        tvArtist.setText(sound.getArtist());
        tvPlayCount.setText("Lượt phát: " + sound.getPlayCount());

        // Đồng bộ icon Play/Pause
        if (position == currentlyPlaying) {
            btnPlayPause.setImageResource(R.drawable.pause);
        } else {
            btnPlayPause.setImageResource(R.drawable.play);
        }

        // Bắt sự kiện Play/Pause
        btnPlayPause.setOnClickListener(v -> {
            if (listener != null) listener.onPlayClick(sound, position, btnPlayPause);
        });

        return convertView;
    }
}
