//package com.example.btlapp_prank.UI;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteConstraintException;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.net.Uri;
//import android.util.Log;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.Locale;
//
//public class DatabaseHelper extends SQLiteOpenHelper {
//
//    private static final String TAG = "DatabaseHelper";
//    private static final String DATABASE_NAME = "UserDB.db";
//    private static final int DATABASE_VERSION = 113;
//
//    private static final String ADMIN_DB_NAME = "AdminDB.db";
//    private static final String DB_PATH_SUFFIX = "/databases/";
//    private final Context mContext;
//
//    public static final String TABLE_USERS = "users";
//    public static final String COLUMN_ID = "id";
//    public static final String COLUMN_EMAIL = "email";
//    public static final String COLUMN_PASSWORD = "password";
//    public static final String COLUMN_LAST_LOGIN = "last_login";
//    public static final String COLUMN_LAST_LOGOUT = "last_logout";
//    public static final String COLUMN_ROLE = "role";
//
//    public static final String TABLE_SOUNDS = "sounds";
//    public static final String COLUMN_SOUND_ID = "sound_id";
//    public static final String COLUMN_TITLE = "title";
//    public static final String COLUMN_ARTIST = "artist";
//    public static final String COLUMN_IMAGE_URI = "image_uri";
//    public static final String COLUMN_AUDIO_URI = "audio_uri";
//
//    public DatabaseHelper(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        this.mContext = context.getApplicationContext();
//        try {
//            copyDatabaseFromAssetsIfNeeded(ADMIN_DB_NAME);
//        } catch (Exception e) {
//            Log.e(TAG, "Lỗi copy AdminDB: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
//                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                COLUMN_EMAIL + " TEXT UNIQUE, " +
//                COLUMN_PASSWORD + " TEXT, " +
//                COLUMN_LAST_LOGIN + " TEXT, " +
//                COLUMN_LAST_LOGOUT + " TEXT, " +
//                COLUMN_ROLE + " TEXT DEFAULT 'user')";
//        db.execSQL(CREATE_USERS_TABLE);
//
//        String CREATE_SOUNDS_TABLE = "CREATE TABLE " + TABLE_SOUNDS + " (" +
//                COLUMN_SOUND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                COLUMN_TITLE + " TEXT, " +
//                COLUMN_ARTIST + " TEXT, " +
//                COLUMN_IMAGE_URI + " TEXT, " +
//                COLUMN_AUDIO_URI + " TEXT)";
//        db.execSQL(CREATE_SOUNDS_TABLE);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (oldVersion < 111) {
//            try {
//                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_ROLE + " TEXT DEFAULT 'user'");
//            } catch (Exception ignored) {}
//        }
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SOUNDS);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
//        onCreate(db);
//    }
//
//    private String getDatabasePathString(String dbName) {
//        return mContext.getApplicationInfo().dataDir + DB_PATH_SUFFIX + dbName;
//    }
//
//    private void copyDatabaseFromAssetsIfNeeded(String assetDbName) throws Exception {
//        File dbFile = mContext.getDatabasePath(assetDbName);
//        if (dbFile != null && dbFile.exists()) return;
//
//        File dbFolder = new File(mContext.getApplicationInfo().dataDir + DB_PATH_SUFFIX);
//        if (!dbFolder.exists()) dbFolder.mkdirs();
//
//        try (InputStream input = mContext.getAssets().open(assetDbName);
//             OutputStream output = new FileOutputStream(getDatabasePathString(assetDbName))) {
//
//            byte[] buffer = new byte[4096];
//            int length;
//            while ((length = input.read(buffer)) > 0) {
//                output.write(buffer, 0, length);
//            }
//            output.flush();
//        }
//    }
//
//    private String getCurrentTimeStamp() {
//        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
//    }
//
//    // ================== USER ==================
//    public boolean insertUser(String email, String password) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_EMAIL, email);
//        values.put(COLUMN_PASSWORD, password);
//        values.putNull(COLUMN_LAST_LOGIN);
//        values.putNull(COLUMN_LAST_LOGOUT);
//        values.put(COLUMN_ROLE, "user");
//        try {
//            long id = db.insertOrThrow(TABLE_USERS, null, values);
//            return id != -1;
//        } catch (SQLiteConstraintException e) {
//            Log.e(TAG, "insertUser failed: " + e.getMessage());
//            return false;
////        } finally {
////            db.close();
//        }
//    }
//
//    public boolean checkUser(String email, String password) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = null;
//        boolean exists = false;
//        try {
//            cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
//                    COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
//                    new String[]{email, password}, null, null, null);
//            exists = cursor != null && cursor.moveToFirst();
//            if (exists) updateLoginTime(email);
//        } catch (Exception e) {
//            Log.e(TAG, "checkUser failed: " + e.getMessage());
//        } finally {
//            if (cursor != null) cursor.close();
////            db.close();
//        }
//        return exists;
//    }
//
//    public void updateLoginTime(String email) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_LAST_LOGIN, getCurrentTimeStamp());
//        try {
//            db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{email});
//        } catch (Exception e) {
//            Log.e(TAG, "updateLoginTime failed: " + e.getMessage());
////        } finally {
////            db.close();
//        }
//    }
//
//    public boolean updateLogoutTime(String email) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_LAST_LOGOUT, getCurrentTimeStamp());
//        int rows = 0;
//        try {
//            rows = db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{email});
//        } catch (Exception e) {
//            Log.e(TAG, "updateLogoutTime failed: " + e.getMessage());
////        } finally {
////            db.close();
//        }
//        return rows > 0;
//    }
//
//    public String getUserRole(String email) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = null;
//        String role = "user";
//        try {
//            cursor = db.query(TABLE_USERS, new String[]{COLUMN_ROLE},
//                    COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE));
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "getUserRole failed: " + e.getMessage());
//        } finally {
//            if (cursor != null) cursor.close();
////            db.close();
//        }
//        return role;
//    }
//
//    public boolean updateRole(String email, String newRole) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_ROLE, newRole);
//        int rows = 0;
//        try {
//            rows = db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{email});
//        } catch (Exception e) {
//            Log.e(TAG, "updateRole failed: " + e.getMessage());
//        } finally {
////            db.close();
//        }
//        return rows > 0;
//    }
//
//    // ================== LOGIN ==================
//    public boolean checkAdmin(String email, String password) {
//        boolean isAdmin = false;
//        SQLiteDatabase adminDb = null;
//        Cursor cursor = null;
//        try {
//            String adminPath = getDatabasePathString(ADMIN_DB_NAME);
//            adminDb = SQLiteDatabase.openDatabase(adminPath, null, SQLiteDatabase.OPEN_READONLY);
//            cursor = adminDb.rawQuery("SELECT role FROM AdminDB WHERE email=? AND password=?",
//                    new String[]{email, password});
//            if (cursor != null && cursor.moveToFirst()) {
//                isAdmin = "admin".equalsIgnoreCase(cursor.getString(0));
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "checkAdmin error: " + e.getMessage());
//        } finally {
//            if (cursor != null) cursor.close();
//            if (adminDb != null) adminDb.close();
//        }
//        return isAdmin;
//    }
//
//    public String checkLogin(String email, String password) {
//        if (checkAdmin(email, password)) return "admin";
//        else if (checkUser(email, password)) return getUserRole(email);
//        return null;
//    }
//
//    // ================== SOUNDS ==================
//    public long insertSound(Sound sound) {
//        if (sound == null) return -1;
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_TITLE, sound.getTitle());
//        values.put(COLUMN_ARTIST, sound.getArtist());
//        values.put(COLUMN_IMAGE_URI, sound.getImageUri() != null ? sound.getImageUri().toString() : null);
//        values.put(COLUMN_AUDIO_URI, sound.getAudioUri() != null ? sound.getAudioUri().toString() : null);
//        long id = -1;
//        try {
//            id = db.insertOrThrow(TABLE_SOUNDS, null, values);
//        } catch (Exception e) {
//            Log.e(TAG, "insertSound failed: " + e.getMessage());
////        } finally {
////            db.close();
//        }
//        return id;
//    }
//
//    public ArrayList<Sound> getAllSounds() {
//        ArrayList<Sound> sounds = new ArrayList<>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = null;
//        try {
//            cursor = db.query(TABLE_SOUNDS, null, null, null, null, null, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                do {
//                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SOUND_ID));
//                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
//                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST));
//                    String imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
//                    String audioUriStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUDIO_URI));
//                    Uri imageUri = imageUriStr != null ? Uri.parse(imageUriStr) : null;
//                    Uri audioUri = audioUriStr != null ? Uri.parse(audioUriStr) : null;
//
//                    Sound sound = new Sound(title, artist, imageUri, audioUri);
//                    sound.setId(id);
//                    sounds.add(sound);
//                } while (cursor.moveToNext());
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "getAllSounds failed: " + e.getMessage());
//        } finally {
//            if (cursor != null) cursor.close();
////            db.close();
//        }
//        return sounds;
//    }
//}