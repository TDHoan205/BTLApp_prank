package com.example.btlapp_prank.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btlapp_prank.R;

public class MainActivitylogin extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnSignUp;
    private Button btnLogin;
    private ImageButton btnX, btnFacebook, btnGoogle;

    private UserDB userDB;
    private AdminDB adminDB;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activitylogin);

        // UI
        editEmail = findViewById(R.id.edtEmailDN);
        editPassword = findViewById(R.id.edtPasswordDN);
        btnLogin = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnGoToSignUp);
        btnX = findViewById(R.id.btnX);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnGoogle = findViewById(R.id.btnGmail);

        // DB & Pref
        userDB = new UserDB(this);
        adminDB = new AdminDB(this);
        prefManager = new PrefManager(this);

        // Chuyển sang SignUp Activity
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainDangKy.class);
            startActivity(intent);
        });

        // Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim().toLowerCase();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Kiểm tra Admin
            if (adminDB.checkAdmin(email, password)) {
                prefManager.saveUserSession(email, "admin");
                Toast.makeText(this, "Đăng nhập với quyền Admin", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            // 2. Kiểm tra User
            else if (userDB.checkUser(email, password)) {
                prefManager.saveUserSession(email, "user");
                Toast.makeText(this, "Đăng nhập với quyền User", Toast.LENGTH_SHORT).show();

                int loginCount = userDB.getLoginCount(email);
                Log.d("LoginActivity", "User " + email + " đã đăng nhập " + loginCount + " lần");

                startActivity(new Intent(this, EnterCodeActivity.class));
                finish();
            }
            // 3. Sai thông tin
            else {
                Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });

        // Liên kết ngoài
        btnX.setOnClickListener(v -> openUrl("https://twitter.com"));
        btnFacebook.setOnClickListener(v -> openUrl("https://facebook.com"));
        btnGoogle.setOnClickListener(v -> openUrl("https://accounts.google.com/"));
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
