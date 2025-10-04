package com.example.btlapp_prank.UI;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class UserDB extends SQLiteOpenHelper {

    private static final String TAG = "UserDB";
    private static final String DATABASE_NAME = "UserDB.db";
    private static final int DATABASE_VERSION = 118; // tăng version để update schema

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_LAST_LOGIN = "last_login";
    public static final String COLUMN_LAST_LOGOUT = "last_logout";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_LOGIN_COUNT = "login_count";

    private SQLiteDatabase mDatabase; // database luôn mở

    public UserDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mDatabase = getWritableDatabase(); // mở database ngay khi khởi tạo
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_LAST_LOGIN + " TEXT, " +
                COLUMN_LAST_LOGOUT + " TEXT, " +
                COLUMN_ROLE + " TEXT DEFAULT 'user', " +
                COLUMN_LOGIN_COUNT + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // thêm cột mới nếu chưa có
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " +
                        COLUMN_LOGIN_COUNT + " INTEGER DEFAULT 0");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " +
                        COLUMN_LAST_LOGOUT + " TEXT");
            } catch (Exception ignored) {}
        }
    }

    private String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // ===== Thêm user mới =====
    public boolean insertUser(String email, String password) {
        if (email == null || password == null) return false;

        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email.trim().toLowerCase());
        values.put(COLUMN_PASSWORD, password.trim());
        values.putNull(COLUMN_LAST_LOGIN);
        values.putNull(COLUMN_LAST_LOGOUT);
        values.put(COLUMN_ROLE, "user");
        values.put(COLUMN_LOGIN_COUNT, 0);

        try {
            long id = mDatabase.insertOrThrow(TABLE_USERS, null, values);
            return id != -1;
        } catch (SQLiteConstraintException e) {
            Log.e(TAG, "insertUser failed: " + e.getMessage());
            return false;
        }
    }

    // ===== Kiểm tra user login =====
    public boolean checkUser(String email, String password) {
        boolean exists = false;
        Cursor cursor = null;

        try {
            cursor = mDatabase.query(TABLE_USERS, new String[]{COLUMN_ID},
                    COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                    new String[]{email.trim().toLowerCase(), password.trim()},
                    null, null, null);

            exists = cursor != null && cursor.moveToFirst();

            if (exists) {
                updateLoginTime(email);
                incrementLoginCount(email);
            }
        } catch (Exception e) {
            Log.e(TAG, "checkUser failed: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return exists;
    }

    // ===== Cập nhật thời gian login =====
    public void updateLoginTime(String email) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGIN, getCurrentTimeStamp());
        mDatabase.update(TABLE_USERS, values, COLUMN_EMAIL + "=?",
                new String[]{email.trim().toLowerCase()});
    }

    // ===== Cập nhật thời gian logout =====
    public boolean updateLogoutTime(String email) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGOUT, getCurrentTimeStamp());
        int rows = mDatabase.update(TABLE_USERS, values, COLUMN_EMAIL + "=?",
                new String[]{email.trim().toLowerCase()});
        return rows > 0;
    }

    // ===== Lấy thời gian logout gần nhất =====
    public String getLastLogout(String email) {
        String logoutTime = null;
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(TABLE_USERS, new String[]{COLUMN_LAST_LOGOUT},
                    COLUMN_EMAIL + "=?", new String[]{email.trim().toLowerCase()},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                logoutTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_LOGOUT));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return logoutTime;
    }

    // ===== Tăng số lần login =====
    public void incrementLoginCount(String email) {
        mDatabase.execSQL("UPDATE " + TABLE_USERS +
                " SET " + COLUMN_LOGIN_COUNT + " = " + COLUMN_LOGIN_COUNT + " + 1 " +
                " WHERE " + COLUMN_EMAIL + "=?", new Object[]{email.trim().toLowerCase()});
    }

    // ===== Lấy số lần login =====
    public int getLoginCount(String email) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(TABLE_USERS, new String[]{COLUMN_LOGIN_COUNT},
                    COLUMN_EMAIL + "=?", new String[]{email.trim().toLowerCase()},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOGIN_COUNT));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }
    // ===== Kiểm tra mật khẩu cũ =====
    public boolean checkPassword(String email, String oldPassword) {
        Cursor cursor = null;
        boolean result = false;
        try {
            cursor = mDatabase.query(TABLE_USERS,
                    new String[]{COLUMN_ID},
                    COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                    new String[]{email.trim().toLowerCase(), oldPassword.trim()},
                    null, null, null);

            result = cursor != null && cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, "checkPassword failed: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    // ===== Cập nhật mật khẩu mới =====
    public boolean updatePassword(String email, String newPassword) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword.trim());
        int rows = mDatabase.update(TABLE_USERS, values,
                COLUMN_EMAIL + "=?", new String[]{email.trim().toLowerCase()});
        return rows > 0;
    }
    // ===== Lấy role =====
    public String getUserRole(String email) {
        String role = "user";
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(TABLE_USERS, new String[]{COLUMN_ROLE},
                    COLUMN_EMAIL + "=?", new String[]{email.trim().toLowerCase()},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return role;
    }

    // ===== Cập nhật role =====
    public boolean updateRole(String email, String newRole) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROLE, newRole.trim());
        int rows = mDatabase.update(TABLE_USERS, values, COLUMN_EMAIL + "=?",
                new String[]{email.trim().toLowerCase()});
        return rows > 0;
    }

    // ===== Lấy tổng số user =====
    public int getAllUsersCount() {
        int count = 0;
        Cursor c = mDatabase.rawQuery("SELECT COUNT(*) AS total FROM " + TABLE_USERS, null);
        if (c.moveToFirst()) count = c.getInt(c.getColumnIndexOrThrow("total"));
        c.close();
        return count;
    }

    // ===== Lấy số lần login của tất cả user, có lọc theo khoảng thời gian =====
    public Map<String, Integer> getUserLoginStats(String from, String to) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String query = "SELECT " + COLUMN_EMAIL + ", " + COLUMN_LOGIN_COUNT + " FROM " + TABLE_USERS;

        if (from != null && to != null) {
            query += " WHERE " + COLUMN_LAST_LOGIN + " BETWEEN ? AND ?";
        }

        Cursor c;
        if (from != null && to != null) {
            c = mDatabase.rawQuery(query, new String[]{from, to});
        } else {
            c = mDatabase.rawQuery(query, null);
        }

        while (c.moveToNext()) {
            String email = c.getString(c.getColumnIndexOrThrow(COLUMN_EMAIL));
            int count = c.getInt(c.getColumnIndexOrThrow(COLUMN_LOGIN_COUNT));
            stats.put(email, count);
        }
        c.close();
        return stats;
    }

    // ===== Đóng database thủ công =====
    public void closeDatabase() {
        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
        }
    }
}
