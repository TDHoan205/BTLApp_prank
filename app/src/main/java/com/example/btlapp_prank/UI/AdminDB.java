package com.example.btlapp_prank.UI;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AdminDB {

    private static final String TAG = "AdminDB";
    private static final String ADMIN_DB_NAME = "AdminDB.db";
    private static final String DB_PATH_SUFFIX = "/databases/";
    private final Context mContext;

    public AdminDB(Context context) {
        this.mContext = context.getApplicationContext();
        try {
            copyDatabaseFromAssetsIfNeeded(ADMIN_DB_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi copy AdminDB: " + e.getMessage());
        }
    }

    private String getDatabasePathString(String dbName) {
        return mContext.getApplicationInfo().dataDir + DB_PATH_SUFFIX + dbName;
    }

    private void copyDatabaseFromAssetsIfNeeded(String assetDbName) throws Exception {
        File dbFile = mContext.getDatabasePath(assetDbName);
        if (dbFile != null && dbFile.exists()) return;

        File dbFolder = new File(mContext.getApplicationInfo().dataDir + DB_PATH_SUFFIX);
        if (!dbFolder.exists()) dbFolder.mkdirs();

        try (InputStream input = mContext.getAssets().open(assetDbName);
             OutputStream output = new FileOutputStream(getDatabasePathString(assetDbName))) {

            byte[] buffer = new byte[4096];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
        }
    }

    // =============== ADMIN LOGIN CHECK ===============
    public boolean checkAdmin(String email, String password) {
        boolean isAdmin = false;
        SQLiteDatabase adminDb = null;
        Cursor cursor = null;
        try {
            String adminPath = getDatabasePathString(ADMIN_DB_NAME);
            adminDb = SQLiteDatabase.openDatabase(adminPath, null, SQLiteDatabase.OPEN_READONLY);
            cursor = adminDb.rawQuery("SELECT role FROM AdminDB WHERE email=? AND password=?",
                    new String[]{email, password});
            if (cursor != null && cursor.moveToFirst()) {
                isAdmin = "admin".equalsIgnoreCase(cursor.getString(0));
            }
        } catch (Exception e) {
            Log.e(TAG, "checkAdmin error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (adminDb != null) adminDb.close();
        }
        return isAdmin;
    }
}
