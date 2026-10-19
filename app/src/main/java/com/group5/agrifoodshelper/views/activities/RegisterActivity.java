package com.group5.agrifoodshelper.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.User;
import com.group5.agrifoodshelper.viewmodels.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtRegEmail, edtPhone, edtRegPassword;
    private TextView btnSubmitRegister, tvBackToLogin;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Ánh xạ giao diện
        edtFullName = findViewById(R.id.edtFullName);
        edtRegEmail = findViewById(R.id.edtRegEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtRegPassword = findViewById(R.id.edtRegPassword);
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // 2. Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 3. Xử lý nút Đăng ký
        btnSubmitRegister.setOnClickListener(v -> {
            String name = edtFullName.getText().toString().trim();
            String email = edtRegEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String pass = edtRegPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi hàm đăng ký thực sự lên Firebase
            authViewModel.registerWithEmail(email, pass, name, phone);
            Toast.makeText(this, "Đang tạo tài khoản...", Toast.LENGTH_SHORT).show();
        });

        // 4. Bắt lỗi từ Firebase (nếu email đã tồn tại, sai định dạng...)
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // 5. Nếu đăng ký thành công -> Chuyển vào trang chủ
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.putExtra("USER_ID", user.getUserId());
                intent.putExtra("USER_NAME", user.getFullName());
                intent.putExtra("USER_ROLE", user.getRole());
                startActivity(intent);
                finish();
            }
        });

        // 6. Xử lý nút Quay lại Đăng nhập
        tvBackToLogin.setOnClickListener(v -> finish());
    }
}