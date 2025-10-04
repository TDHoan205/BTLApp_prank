package com.example.btlapp_prank.UI;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SoundDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SoundDB.db";
    private static final int DATABASE_VERSION = 5; // tăng version để upgrade

    // ===== Bảng Sound =====
    private static final String TABLE_SOUNDS = "sounds";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_ARTIST = "artist";
    private static final String COLUMN_IMAGE_URI = "image_uri";
    private static final String COLUMN_AUDIO_URI = "audio_uri";
    private static final String COLUMN_CREATED_BY = "created_by";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";
    private static final String COLUMN_DELETED_AT = "deleted_at";
    private static final String COLUMN_PLAY_COUNT = "play_count";
    private static final String COLUMN_CATEGORY = "category"; // 🔹 Chủ đề

    // ===== Bảng Category =====
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COLUMN_CATEGORY_ID = "id";
    private static final String COLUMN_CATEGORY_NAME = "name";

    private final SQLiteDatabase mDatabase; // giữ luôn mở

    public SoundDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mDatabase = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SOUNDS = "CREATE TABLE " + TABLE_SOUNDS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_ARTIST + " TEXT, " +
                COLUMN_IMAGE_URI + " TEXT, " +
                COLUMN_AUDIO_URI + " TEXT, " +
                COLUMN_CREATED_BY + " TEXT, " +
                COLUMN_CREATED_AT + " TEXT, " +
                COLUMN_UPDATED_AT + " TEXT, " +
                COLUMN_DELETED_AT + " TEXT, " +
                COLUMN_PLAY_COUNT + " INTEGER DEFAULT 0, " +
                COLUMN_CATEGORY + " TEXT" +
                ")";
        db.execSQL(CREATE_SOUNDS);

        String CREATE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY_NAME + " TEXT UNIQUE)";
        db.execSQL(CREATE_CATEGORIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_SOUNDS + " ADD COLUMN " + COLUMN_CATEGORY + " TEXT");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " (" +
                    COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CATEGORY_NAME + " TEXT UNIQUE)");
        }
    }

    private String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // ===== Thêm nhạc có category =====
    public long addSound(Sound sound, String category) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, sound.getTitle());
        values.put(COLUMN_ARTIST, sound.getArtist());
        values.put(COLUMN_IMAGE_URI, sound.getImageUri() != null ? sound.getImageUri().toString() : null);
        values.put(COLUMN_AUDIO_URI, sound.getAudioUri() != null ? sound.getAudioUri().toString() : null);
        values.put(COLUMN_CREATED_BY, sound.getCreatedBy());
        values.put(COLUMN_CREATED_AT, getCurrentTimeStamp());
        values.putNull(COLUMN_UPDATED_AT);
        values.putNull(COLUMN_DELETED_AT);
        values.put(COLUMN_PLAY_COUNT, sound.getPlayCount());
        values.put(COLUMN_CATEGORY, category);

        return mDatabase.insert(TABLE_SOUNDS, null, values);
    }

    // ===== Thêm nhạc không cần category =====
    public long addSound(Sound sound) {
        return addSound(sound, null); // mặc định category = null
    }

    // ===== Lấy nhạc theo ID =====
    public Sound getSound(int id) {
        try (Cursor cursor = mDatabase.query(
                TABLE_SOUNDS, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                return new Sound(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST)),
                        parseUri(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))),
                        parseUri(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUDIO_URI))),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_BY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELETED_AT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAY_COUNT))
                );
            }
        }
        return null;
    }

    // ===== Lấy tất cả nhạc (chưa xóa) =====
    public List<Sound> getAllSounds() {
        List<Sound> soundList = new ArrayList<>();
        try (Cursor cursor = mDatabase.rawQuery(
                "SELECT * FROM " + TABLE_SOUNDS + " WHERE " + COLUMN_DELETED_AT + " IS NULL", null)) {

            if (cursor.moveToFirst()) {
                do {
                    Sound sound = new Sound(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST)),
                            parseUri(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))),
                            parseUri(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUDIO_URI))),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_BY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELETED_AT)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAY_COUNT))
                    );
                    soundList.add(sound);
                } while (cursor.moveToNext());
            }
        }
        return soundList;
    }

    // ===== Lấy nhạc theo Category =====
    public List<Sound> getSoundsByCategory(String category) {
        List<Sound> soundList = new ArrayList<>();
        try (Cursor cursor = mDatabase.query(
                TABLE_SOUNDS, null, COLUMN_CATEGORY + "=? AND " + COLUMN_DELETED_AT + " IS NULL",
                new String[]{category}, null, null, null)) {

            if (cursor.moveToFirst()) {
                do {
                    Sound sound = new Sound(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST)),
                            parseUri(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))),
                            parseUri(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUDIO_URI))),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_BY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELETED_AT)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAY_COUNT))
                    );
                    soundList.add(sound);
                } while (cursor.moveToNext());
            }
        }
        return soundList;
    }

    // ===== Cập nhật nhạc =====
    public int updateSound(Sound sound) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, sound.getTitle());
        values.put(COLUMN_ARTIST, sound.getArtist());
        values.put(COLUMN_IMAGE_URI, sound.getImageUri() != null ? sound.getImageUri().toString() : null);
        values.put(COLUMN_AUDIO_URI, sound.getAudioUri() != null ? sound.getAudioUri().toString() : null);
        values.put(COLUMN_UPDATED_AT, getCurrentTimeStamp());
        values.put(COLUMN_PLAY_COUNT, sound.getPlayCount());
        values.put(COLUMN_CATEGORY, sound.getCreatedBy()); // ⚠ nếu muốn category riêng thì đổi field khác

        return mDatabase.update(TABLE_SOUNDS, values, COLUMN_ID + "=?", new String[]{String.valueOf(sound.getId())});
    }

    // ===== Tăng số lần phát nhạc trực tiếp bằng SQL =====
    public void incrementPlayCount(int id) {
        mDatabase.execSQL("UPDATE " + TABLE_SOUNDS +
                " SET " + COLUMN_PLAY_COUNT + " = " + COLUMN_PLAY_COUNT + " + 1" +
                " WHERE " + COLUMN_ID + " = ?", new Object[]{id});
    }

    // ===== Xóa nhạc (soft delete) =====
    public void deleteSound(int id) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DELETED_AT, getCurrentTimeStamp());
        mDatabase.update(TABLE_SOUNDS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    // ===== Quản lý Category =====
    public long addCategory(String name) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        return mDatabase.insert(TABLE_CATEGORIES, null, values);
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        try (Cursor cursor = mDatabase.query(TABLE_CATEGORIES, null, null, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    categories.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)));
                } while (cursor.moveToNext());
            }
        }
        return categories;
    }

    public void deleteCategory(String name) {
        mDatabase.delete(TABLE_CATEGORIES, COLUMN_CATEGORY_NAME + "=?", new String[]{name});
    }

    // ===== Đóng DB khi không cần nữa =====
    public void closeDatabase() {
        if (mDatabase.isOpen()) {
            mDatabase.close();
        }
    }

    // ===== Hàm phụ: convert String -> Uri =====
    private Uri parseUri(String uriString) {
        return (uriString != null) ? Uri.parse(uriString) : null;
    }
}
