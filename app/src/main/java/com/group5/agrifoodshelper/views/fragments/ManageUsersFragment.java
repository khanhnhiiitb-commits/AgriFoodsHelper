package com.group5.agrifoodshelper.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.User;
import com.group5.agrifoodshelper.views.adapters.UserAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersFragment extends Fragment {

    private RecyclerView recyclerUsers;
    private UserAdapter userAdapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_manage_users, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        ImageView btnBackFromUsers =
                view.findViewById(R.id.btnBackFromUsers);

        if (btnBackFromUsers != null) {
            btnBackFromUsers.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        recyclerUsers = view.findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));

        userAdapter = new UserAdapter();
        recyclerUsers.setAdapter(userAdapter);

        loadUsers();
    }

    private void loadUsers() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .addSnapshotListener((snapshots, error) -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (error != null) {
                        Log.e("ManageUsersFragment",
                                "Lỗi tải danh sách users", error);

                        Toast.makeText(requireContext(),
                                "Không thể tải người dùng: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots == null) {
                        return;
                    }

                    Log.d("ManageUsersFragment",
                            "Số document users: " + snapshots.size());

                    List<User> users = new ArrayList<>();

                    for (QueryDocumentSnapshot document : snapshots) {
                        String userId = document.getString("userId");

                        if (userId == null || userId.isEmpty()) {
                            userId = document.getId();
                        }

                        String fullName = document.getString("fullName");
                        String phone = document.getString("phone");
                        String role = document.getString("role");
                        Double trustScore = document.getDouble("trustScore");

                        User user = new User(
                                userId,
                                fullName == null ? "" : fullName,
                                phone == null ? "" : phone,
                                role == null ? "" : role,
                                trustScore == null ? 0.0 : trustScore
                        );

                        users.add(user);
                    }

                    userAdapter.setUserList(users);
                });
    }
}
