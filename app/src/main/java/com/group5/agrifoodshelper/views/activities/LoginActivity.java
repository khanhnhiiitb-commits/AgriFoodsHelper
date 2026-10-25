package com.group5.agrifoodshelper.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.User;
import com.group5.agrifoodshelper.viewmodels.AuthViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    // Khai báo thêm các biến giao diện
    private LinearLayout btnGoogleSignIn;
    private TextView tvRegister, btnLogin, tvForgotPassword;
    private EditText edtEmail, edtPassword;

    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Ánh xạ toàn bộ UI
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvRegister = findViewById(R.id.tvRegister);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. Xử lý nút Đăng ký (Chuyển trang)
        tvRegister.setOnClickListener(v -> {
            Toast.makeText(this, "Chuyển sang trang Đăng ký...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ Email và Mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.loginWithEmail(email, pass);
            Toast.makeText(this, "Đang kiểm tra thông tin...", Toast.LENGTH_SHORT).show();
        });

        // 4. Cấu hình Google Sign-In (Code gốc của bạn)
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, options);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK) {
                        Toast.makeText(this, "Bạn đã hủy đăng nhập Google.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent data = result.getData();
                    if (data == null) {
                        Toast.makeText(this, "Không nhận được kết quả đăng nhập.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null && account.getIdToken() != null) {
                            authViewModel.loginWithGoogle(account.getIdToken());
                        } else {
                            Toast.makeText(this, "Không lấy được Google ID token.", Toast.LENGTH_LONG).show();
                        }
                    } catch (ApiException e) {
                        Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
                    }
                }
        );

        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // 5. Theo dõi lỗi từ AuthViewModel
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });


        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUserId())
                        .get()
                        .addOnSuccessListener(document -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            if (!document.exists()) {
                                FirebaseAuth.getInstance().signOut();

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Không tìm thấy thông tin tài khoản",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            Boolean isActive = document.getBoolean("isActive");

                            if (Boolean.FALSE.equals(isActive)) {
                                FirebaseAuth.getInstance().signOut();

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Tài khoản đã bị khóa",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            openMainActivity(user);
                        })
                        .addOnFailureListener(e -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            FirebaseAuth.getInstance().signOut();

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Không thể kiểm tra trạng thái tài khoản",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
            }
        });

    }

    private void openMainActivity(User user) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("USER_ID", user.getUserId());
        intent.putExtra("USER_NAME", user.getFullName());
        intent.putExtra("USER_ROLE", user.getRole());
        startActivity(intent);
        finish();
    }
}