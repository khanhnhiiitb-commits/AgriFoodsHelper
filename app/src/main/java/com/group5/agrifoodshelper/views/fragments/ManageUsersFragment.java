package com.group5.agrifoodshelper.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.group5.agrifoodshelper.R;

public class ManageUsersFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Liên kết với file giao diện bạn đã tạo lúc nãy
        return inflater.inflate(R.layout.fragment_manage_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Xử lý sự kiện cho nút mũi tên Quay lại
        ImageView btnBackFromUsers = view.findViewById(R.id.btnBackFromUsers);
        if (btnBackFromUsers != null) {
            btnBackFromUsers.setOnClickListener(v -> {
                // Lệnh popBackStack sẽ đóng Fragment hiện tại và quay về màn hình trước đó (Profile)
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }
}