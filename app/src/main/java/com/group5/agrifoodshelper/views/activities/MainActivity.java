package com.group5.agrifoodshelper.views.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group5.agrifoodshelper.R;

import com.group5.agrifoodshelper.views.fragments.HomeFeedFragment;
import com.group5.agrifoodshelper.views.fragments.JobBoardFragment;
import com.group5.agrifoodshelper.views.fragments.CreateReportFragment;
import com.group5.agrifoodshelper.views.fragments.ProfileFragment;
import com.group5.agrifoodshelper.views.fragments.SupportBoardFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Mặc định tải trang chủ (Bảng tin) đầu tiên
        if (savedInstanceState == null) {
            loadFragment(new HomeFeedFragment());
        }

        // Xử lý sự kiện click cho các nút trên menu
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // 1. ĐỌC VAI TRÒ HIỆN TẠI TỪ BỘ NHỚ ĐỆM
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String currentRole = prefs.getString("role", "SUPPORTER"); // Mặc định là SUPPORTER

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFeedFragment();

            } else if (itemId == R.id.nav_coordinate) {
                // 2. CHỈ TÀI XẾ (DRIVER) VÀ ADMIN ĐƯỢC NHẬN CHUYẾN
                if (!currentRole.equalsIgnoreCase("DRIVER") && !currentRole.equalsIgnoreCase("ADMIN")) {
                    Toast.makeText(this, "Chỉ Tài xế mới được truy cập Bảng nhận chuyến!", Toast.LENGTH_SHORT).show();
                    return false; // Trả về false để chặn việc sáng icon và đổi tab
                }
                selectedFragment = new JobBoardFragment();

            } else if (itemId == R.id.nav_post) {
                // 3. TÀI XẾ KHÔNG ĐƯỢC ĐĂNG BÀI (Chỉ Nông dân, Supporter, Admin được đăng)
                if (currentRole.equalsIgnoreCase("DRIVER")) {
                    Toast.makeText(this, "Tài xế không có quyền tạo bài đăng giải cứu!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                selectedFragment = new CreateReportFragment();

            } else if (itemId == R.id.nav_order) {
                // 4. CHỈ SUPPORTER VÀ ADMIN ĐƯỢC NHẬN VIỆC HỖ TRỢ VƯỜN
                if (!currentRole.equalsIgnoreCase("SUPPORTER") && !currentRole.equalsIgnoreCase("ADMIN")) {
                    Toast.makeText(this, "Chỉ Tình nguyện viên mới được nhận việc hỗ trợ!", Toast.LENGTH_SHORT).show();
                    return false;
                }

                selectedFragment = new SupportBoardFragment();

            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            // Tráo đổi màn hình nếu Fragment hợp lệ
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}