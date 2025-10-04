package com.example.btlapp_prank.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btlapp_prank.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainDangKy extends AppCompatActivity {

    private TextInputEditText edtEmailDK, edtPasswordDK, edtConfirmPasswordDK;
    private MaterialButton btnSignUp;
    private ImageButton btnBack, btnX, btnFacebook, btnGmail;

    // DB
    private UserDB userDB;
    private AdminDB adminDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dang_ky);

        // Ánh xạ view
        edtEmailDK = findViewById(R.id.edtEmailDK);
        edtPasswordDK = findViewById(R.id.edtPasswordDK);
        edtConfirmPasswordDK = findViewById(R.id.edtConfirmPasswordDK);

        btnSignUp = findViewById(R.id.btnSignUp);
        btnBack = findViewById(R.id.btnBack);

        btnX = findViewById(R.id.btnX);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnGmail = findViewById(R.id.btnGmail);

        // Khởi tạo DB
        userDB = new UserDB(this);
        adminDB = new AdminDB(this);

        // Quay lại màn hình Login
        btnBack.setOnClickListener(v -> {
            // Dùng finish() để quay lại Activity trước đó (MainActivitylogin)
            finish();
        });

        // Đăng ký
        btnSignUp.setOnClickListener(v -> {
            String email = edtEmailDK.getText().toString().trim().toLowerCase();
            String password = edtPasswordDK.getText().toString().trim();
            String confirm = edtConfirmPasswordDK.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra AdminDB
            if (adminDB.checkAdmin(email, password)) {
                Toast.makeText(this, "Email này thuộc Admin, không thể đăng ký!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thêm vào UserDB
            boolean inserted = userDB.insertUser(email, password);
            if (inserted) {
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                // Sau khi đăng ký thành công → quay về Login Activity
                Intent intent = new Intent(MainDangKy.this, MainActivitylogin.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Email đã tồn tại trong UserDB", Toast.LENGTH_SHORT).show();
            }
        });

        // Liên kết mạng xã hội
        btnX.setOnClickListener(v -> openUrl("https://twitter.com"));
        btnFacebook.setOnClickListener(v -> openUrl("https://facebook.com"));
        btnGmail.setOnClickListener(v -> openUrl("https://accounts.google.com/"));
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
