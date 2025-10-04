package com.example.btlapp_prank.UI;

import android.net.Uri;
import java.io.File;

public class Sound {
    private int id;
    private String title;
    private String artist;
    private String imageUriString;
    private String audioUriString;

    private String createdBy;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    private int playCount;

    // Constructor dùng khi thêm mới
    public Sound(String title, String artist, Uri imageUri, Uri audioUri, String createdBy) {
        this.title = title;
        this.artist = artist;
        this.imageUriString = (imageUri != null) ? imageUri.toString() : null;
        this.audioUriString = (audioUri != null) ? audioUri.toString() : null;
        this.createdBy = createdBy;
        this.playCount = 0;
    }

    // Constructor đầy đủ (đọc từ DB)
    public Sound(int id, String title, String artist, Uri imageUri, Uri audioUri,
                 String createdBy, String createdAt, String updatedAt, String deletedAt,
                 int playCount) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.imageUriString = (imageUri != null) ? imageUri.toString() : null;
        this.audioUriString = (audioUri != null) ? audioUri.toString() : null;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.playCount = playCount;
    }

    // ===== Getter/Setter =====
    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public Uri getImageUri() { return (imageUriString != null) ? Uri.parse(imageUriString) : null; }
    public void setImageUri(Uri imageUri) { this.imageUriString = (imageUri != null) ? imageUri.toString() : null; }

    public Uri getAudioUri() { return (audioUriString != null) ? Uri.parse(audioUriString) : null; }
    public void setAudioUri(Uri audioUri) { this.audioUriString = (audioUri != null) ? audioUri.toString() : null; }

    // ✅ Lấy tên file từ URI
    public String getAudioFileName() {
        if (audioUriString == null) return "unknown";
        Uri uri = Uri.parse(audioUriString);
        String path = uri.getPath();
        if (path == null) return "unknown";
        return new File(path).getName();
    }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getDeletedAt() { return deletedAt; }
    public void setDeletedAt(String deletedAt) { this.deletedAt = deletedAt; }

    @Override
    public String toString() {
        return "Sound{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", playCount=" + playCount +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", deletedAt='" + deletedAt + '\'' +
                '}';
    }
}
