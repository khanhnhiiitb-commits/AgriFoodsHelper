package com.group5.agrifoodshelper.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.views.activities.LoginActivity;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Chỉ inflate giao diện ở đây
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View thông tin cá nhân
        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvProfileRole = view.findViewById(R.id.tvProfileRole);
        TextView btnLogout = view.findViewById(R.id.btnLogout);
        LinearLayout layoutAdminArea = view.findViewById(R.id.layoutAdminArea);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            String currentUserId = auth.getCurrentUser().getUid();
            // Lấy email trực tiếp từ Auth
            tvProfileEmail.setText(auth.getCurrentUser().getEmail());

            db.collection("Users").document(currentUserId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            String name = documentSnapshot.getString("fullName"); // Thay bằng tên biến lưu tên của bạn

                            // Cập nhật Tên và Vai trò thật
                            if (name != null) tvProfileName.setText(name);
                            if (role != null) tvProfileRole.setText("Vai trò: " + role);

                            // KIỂM TRA QUYỀN ADMIN
                            if ("ADMIN".equals(role)) {
                                layoutAdminArea.setVisibility(View.VISIBLE);

                                TextView btnManageUsers = view.findViewById(R.id.btnManageUsers);
                                TextView btnApproveCampaigns = view.findViewById(R.id.btnApproveCampaigns);

                                // Bấm nút Quản lý người dùng -> Chuyển sang ManageUsersFragment
                                btnManageUsers.setOnClickListener(v -> {
                                    requireActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, new ManageUsersFragment())
                                            .addToBackStack(null) // Cho phép bấm nút Back để quay lại Profile
                                            .commit();
                                });

                                // Bấm nút Duyệt chiến dịch -> Chuyển sang AdminModFragment
                                btnApproveCampaigns.setOnClickListener(v -> {
                                    requireActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, new PendingCampaignsFragment())
                                            .addToBackStack(null)
                                            .commit();
                                });
                            } else {
                                layoutAdminArea.setVisibility(View.GONE);
                            }
                        }
                    });
        }

        // Xử lý Đăng xuất
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            if (getActivity() != null) {
                android.content.Intent intent = new android.content.Intent(getActivity(), LoginActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}