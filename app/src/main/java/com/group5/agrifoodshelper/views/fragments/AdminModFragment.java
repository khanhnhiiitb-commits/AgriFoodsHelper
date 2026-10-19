package com.group5.agrifoodshelper.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.group5.agrifoodshelper.R;

public class AdminModFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_mod, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ 2 nút menu của bảng điều khiển
        TextView btnManageUsers = view.findViewById(R.id.btnManageUsers);
        TextView btnApproveCampaigns = view.findViewById(R.id.btnApproveCampaigns);

        // 2. Xử lý nút Duyệt chiến dịch -> Mở danh sách PENDING
        if (btnApproveCampaigns != null) {
            btnApproveCampaigns.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PendingCampaignsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // 3. Xử lý nút Quản lý người dùng
        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ManageUsersFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }
}