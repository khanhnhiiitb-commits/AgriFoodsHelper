package com.group5.agrifoodshelper.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.views.activities.LoginActivity;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Khởi tạo view từ file giao diện XML
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Tìm nút Đăng xuất thông qua ID
        TextView btnLogout = view.findViewById(R.id.btnLogout);

        // Bắt sự kiện khi người dùng bấm vào nút Đăng xuất
        btnLogout.setOnClickListener(v -> {
            // 1. Lệnh đăng xuất tài khoản khỏi Firebase
            FirebaseAuth.getInstance().signOut();

            // 2. Chuyển người dùng quay ngược lại trang LoginActivity
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                // Xóa lịch sử các trang trước đó để người dùng không thể bấm phím "Back" để quay lại app
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        return view;
    }
}