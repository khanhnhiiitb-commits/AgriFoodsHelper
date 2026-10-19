package com.group5.agrifoodshelper.views.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group5.agrifoodshelper.R;

// Import các Fragment của bạn
import com.group5.agrifoodshelper.views.fragments.HomeFeedFragment;
import com.group5.agrifoodshelper.views.fragments.JobBoardFragment;
import com.group5.agrifoodshelper.views.fragments.CreateReportFragment;
import com.group5.agrifoodshelper.views.fragments.ProfileFragment;

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

        // Xử lý sự kiện click cho 5 nút trên menu
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFeedFragment();
            } else if (itemId == R.id.nav_coordinate) {
                // Tạm dùng JobBoardFragment cho nút Điều phối
                selectedFragment = new JobBoardFragment();
            } else if (itemId == R.id.nav_post) {
                // Tạm dùng CreateReportFragment cho nút Đăng bài
                selectedFragment = new CreateReportFragment();
            } else if (itemId == R.id.nav_order) {
                // Nút Đơn hàng: Nếu chưa có Fragment thì tạm thời để trống
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            // Tráo đổi màn hình nếu Fragment hợp lệ
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;

        });
        // Dán tạm đoạn này vào MainActivity.java (dưới dòng setContentView) để đẩy dữ liệu mẫu lên
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

// 1. Tạo một object Product giả (bạn thay đổi tham số cho khớp với constructor Product của bạn)
// Ví dụ: Product(id, name, unit, price, total, sold...)
        com.group5.agrifoodshelper.models.Product dummyProduct = new com.group5.agrifoodshelper.models.Product();

// 2. Tạo đối tượng Campaign giả bằng constructor đầy đủ của bạn
        com.group5.agrifoodshelper.models.Campaign dummyCampaign = new com.group5.agrifoodshelper.models.Campaign(
                "CAMP_" + System.currentTimeMillis(), // campaignId
                "USER_123",                           // authorId
                "Nguyễn Văn Nông",                    // authorName
                "0901234567",                         // contactPhone
                "Giải cứu Thanh Long Ruột Đỏ Bình Thuận", // title
                "Nông dân Bình Thuận đang gặp khó khăn do không thể xuất khẩu...", // story
                "Hàm Thuận Nam, Bình Thuận",          // address
                "URGENT_24H",                         // priority (Để test cờ khẩn cấp đỏ)
                "ACTIVE",                             // status
                true,                                 // pinned
                dummyProduct,                         // product
                new java.util.ArrayList<>(),          // imageUrls (mảng rỗng)
                24,                                   // likeCount
                5,                                    // commentCount
                System.currentTimeMillis()            // createdAt
        );

// Bổ sung số lượng để test ProgressBar
        dummyCampaign.setTargetQuantity(2000.0);
        dummyCampaign.setCurrentQuantity(1150.0);

// 3. Đẩy thẳng lên Firebase
        db.collection("Campaigns")
                .document(dummyCampaign.getCampaignId())
                .set(dummyCampaign)
                .addOnSuccessListener(aVoid -> {
                    android.widget.Toast.makeText(this, "Đã bơm dữ liệu mẫu lên Firebase thành công!", android.widget.Toast.LENGTH_LONG).show();
                });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}