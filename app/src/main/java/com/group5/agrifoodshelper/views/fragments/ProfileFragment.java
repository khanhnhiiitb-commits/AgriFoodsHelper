package com.group5.agrifoodshelper.views.fragments;

import android.content.Context;
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvProfileRole = view.findViewById(R.id.tvProfileRole);
        TextView btnLogout = view.findViewById(R.id.btnLogout);
        LinearLayout layoutAdminArea = view.findViewById(R.id.layoutAdminArea);

        // 1. ÁNH XẠ NÚT NÂNG CẤP ROLE
        com.google.android.material.button.MaterialButton btnUpgradeRole = view.findViewById(R.id.btnUpgradeRole);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 2. BẮT SỰ KIỆN CHUYỂN TRANG CHO NÚT NÂNG CẤP
        if (btnUpgradeRole != null) {
            btnUpgradeRole.setOnClickListener(v -> {
                startActivity(new android.content.Intent(getActivity(), com.group5.agrifoodshelper.views.activities.UpgradeRoleActivity.class));
            });
        }

        if (auth.getCurrentUser() != null) {
            String currentUserId = auth.getCurrentUser().getUid();
            tvProfileEmail.setText(auth.getCurrentUser().getEmail());

            db.collection("users").document(currentUserId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            String name = documentSnapshot.getString("fullName");

                            if (name != null) tvProfileName.setText(name);
                            if (role != null) tvProfileRole.setText("Vai trò: " + role);
                            if (getContext() != null && role != null) {
                                android.content.SharedPreferences.Editor editor = getContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE).edit();
                                editor.putString("role", role);
                                editor.apply();
                            }

                            // KIỂM TRA QUYỀN ADMIN
                            if ("ADMIN".equalsIgnoreCase(role)) {
                                layoutAdminArea.setVisibility(View.VISIBLE);
                                // Tùy chọn: Ẩn nút đăng ký đối tác đối với Admin
                                if (btnUpgradeRole != null) btnUpgradeRole.setVisibility(View.GONE);

                                TextView btnManageUsers = view.findViewById(R.id.btnManageUsers);
                                TextView btnApproveCampaigns = view.findViewById(R.id.btnApproveCampaigns);
                                TextView btnManageRequests = view.findViewById(R.id.btnManageRequests);

                                btnManageUsers.setOnClickListener(v -> {
                                    requireActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, new ManageUsersFragment())
                                            .addToBackStack(null)
                                            .commit();
                                });

                                btnApproveCampaigns.setOnClickListener(v -> {
                                    requireActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, new PendingCampaignsFragment())
                                            .addToBackStack(null)
                                            .commit();
                                });

                                if (btnManageRequests != null) {
                                    btnManageRequests.setOnClickListener(v -> {
                                        requireActivity().getSupportFragmentManager().beginTransaction()
                                                .replace(R.id.fragment_container, new ManageRoleRequestsFragment())
                                                .addToBackStack(null)
                                                .commit();
                                    });
                                }

                            } else {
                                layoutAdminArea.setVisibility(View.GONE);
                            }
                        }
                    });
        }

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            if (getActivity() != null) {
                android.content.Intent intent = new android.content.Intent(getActivity(), LoginActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit().clear().apply();
            }
        });
    }
}