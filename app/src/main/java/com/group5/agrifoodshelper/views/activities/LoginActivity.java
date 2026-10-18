package com.group5.agrifoodshelper.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
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

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;
    private LinearLayout btnGoogleSignIn;

    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        authViewModel = new ViewModelProvider(this)
                .get(AuthViewModel.class);

        // Cấu hình Google Sign-In
        GoogleSignInOptions options =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN
                )
                        .requestIdToken(
                                getString(R.string.default_web_client_id)
                        )
                        .requestEmail()
                        .build();

        googleSignInClient =
                GoogleSignIn.getClient(this, options);

        // Nhận kết quả sau khi người dùng chọn tài khoản Google
        googleSignInLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode()
                                    != RESULT_OK) {
                                Toast.makeText(
                                        this,
                                        "Bạn đã hủy đăng nhập Google.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            Intent data = result.getData();

                            if (data == null) {
                                Toast.makeText(
                                        this,
                                        "Không nhận được kết quả đăng nhập.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            Task<GoogleSignInAccount> task =
                                    GoogleSignIn.getSignedInAccountFromIntent(
                                            data
                                    );

                            try {
                                GoogleSignInAccount account =
                                        task.getResult(ApiException.class);

                                if (account != null
                                        && account.getIdToken() != null) {

                                    authViewModel.loginWithGoogle(
                                            account.getIdToken()
                                    );

                                } else {
                                    Toast.makeText(
                                            this,
                                            "Không lấy được Google ID token.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }

                            } catch (ApiException e) {
                                Toast.makeText(
                                        this,
                                        "Đăng nhập Google thất bại: "
                                                + e.getStatusCode(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );

        // Nhấn nút Google
        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent =
                    googleSignInClient.getSignInIntent();

            googleSignInLauncher.launch(signInIntent);
        });

        // Theo dõi lỗi từ AuthViewModel
        authViewModel.getErrorMessage().observe(
                this,
                error -> {
                    if (error != null && !error.isEmpty()) {
                        Toast.makeText(
                                this,
                                error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        // Đăng nhập thành công và tải được role
        authViewModel.getCurrentUser().observe(
                this,
                user -> {
                    if (user != null) {
                        openMainActivity(user);
                    }
                }
        );
    }

    private void openMainActivity(User user) {
        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        intent.putExtra("USER_ID", user.getUserId());
        intent.putExtra("USER_NAME", user.getFullName());
        intent.putExtra("USER_ROLE", user.getRole());

        startActivity(intent);
        finish();
    }
}
