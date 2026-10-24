package com.group5.agrifoodshelper.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.RoleRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoleRequestAdapter extends RecyclerView.Adapter<RoleRequestAdapter.RequestViewHolder> {

    private List<RoleRequest> requestList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void setRequestList(List<RoleRequest> list) {
        this.requestList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_role_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RoleRequest request = requestList.get(position);

        holder.tvRequestedRole.setText("Yêu cầu lên: " + request.getRequestedRole());

        // Chuyển Map thông tin thành chuỗi dễ đọc
        StringBuilder details = new StringBuilder();
        details.append("User ID: ").append(request.getUserId()).append("\n");
        if (request.getInfo() != null) {
            for (Map.Entry<String, String> entry : request.getInfo().entrySet()) {
                details.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        holder.tvRequestDetails.setText(details.toString());

        // Xử lý Phê duyệt
        holder.btnApprove.setOnClickListener(v -> {
            // 1. Cập nhật role trong bảng users
            db.collection("users").document(request.getUserId())
                    .update("role", request.getRequestedRole())
                    .addOnSuccessListener(aVoid -> {
                        // 2. Đổi trạng thái đơn thành APPROVED
                        db.collection("role_requests").document(request.getRequestId())
                                .update("status", "APPROVED")
                                .addOnSuccessListener(aVoid2 -> {
                                    requestList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, requestList.size());
                                    Toast.makeText(v.getContext(), "Đã cấp quyền " + request.getRequestedRole(), Toast.LENGTH_SHORT).show();
                                });
                    });
        });

        // Xử lý Từ chối
        holder.btnReject.setOnClickListener(v -> {
            db.collection("role_requests").document(request.getRequestId())
                    .update("status", "REJECTED")
                    .addOnSuccessListener(aVoid -> {
                        requestList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, requestList.size());
                        Toast.makeText(v.getContext(), "Đã từ chối yêu cầu", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvRequestedRole, tvRequestDetails;
        MaterialButton btnApprove, btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRequestedRole = itemView.findViewById(R.id.tvRequestedRole);
            tvRequestDetails = itemView.findViewById(R.id.tvRequestDetails);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}