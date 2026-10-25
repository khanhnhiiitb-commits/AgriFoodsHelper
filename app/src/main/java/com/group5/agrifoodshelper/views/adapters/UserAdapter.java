package com.group5.agrifoodshelper.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private final List<String> lockedUserIds = new ArrayList<>();

    public void setUserList(List<User> users) {
        userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_user, parent, false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull UserViewHolder holder, int position) {

        User user = userList.get(position);

        holder.txtUserName.setText(
                user.getFullName() == null ? "Chưa có tên" : user.getFullName());

        holder.txtUserPhone.setText(
                "SĐT: " + (user.getPhone() == null ? "Chưa cập nhật" : user.getPhone()));

        holder.txtUserRole.setText(
                "Vai trò: " + (user.getRole() == null ? "Chưa xác định" : user.getRole()));

        String userId = user.getUserId();

        if (lockedUserIds.contains(userId)) {
            holder.btnLockUser.setText("Đã khóa");
            holder.btnLockUser.setEnabled(false);
        } else {
            holder.btnLockUser.setText("Khóa tài khoản");
            holder.btnLockUser.setEnabled(true);
        }

        holder.btnLockUser.setOnClickListener(v -> {
            if (userId == null || userId.trim().isEmpty()) {
                Toast.makeText(v.getContext(),
                        "Không tìm thấy ID người dùng",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Không cho quản trị viên tự khóa tài khoản đang đăng nhập.
            com.google.firebase.auth.FirebaseUser currentUser =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null && userId.equals(currentUser.getUid())) {
                Toast.makeText(v.getContext(),
                        "Bạn không thể tự khóa tài khoản đang đăng nhập",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            holder.btnLockUser.setEnabled(false);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("isActive", false)
                    .addOnSuccessListener(unused -> {
                        lockedUserIds.add(userId);
                        holder.btnLockUser.setText("Đã khóa");

                        Toast.makeText(v.getContext(),
                                "Đã khóa tài khoản",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        holder.btnLockUser.setEnabled(true);

                        Toast.makeText(v.getContext(),
                                "Khóa thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView txtUserName, txtUserPhone, txtUserRole;
        Button btnLockUser;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserPhone = itemView.findViewById(R.id.txtUserPhone);
            txtUserRole = itemView.findViewById(R.id.txtUserRole);
            btnLockUser = itemView.findViewById(R.id.btnLockUser);
        }
    }
}
