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
        if (requestList == null || position < 0 || position >= requestList.size()) {
            return;
        }
        RoleRequest request = requestList.get(position);
        if (request == null) return;
        if (holder.tvRequestedRole != null) {
            holder.tvRequestedRole.setText("Yêu cầu lên: " + request.getRequestedRole());
        }
        // Chuyển Map thông tin thành chuỗi dễ đọc
        if (holder.tvRequestDetails != null) {
        StringBuilder details = new StringBuilder();
        details.append("User ID: ").append(request.getUserId()).append("\n");
        if (request.getInfo() != null) {
            for (Map.Entry<String, String> entry : request.getInfo().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    details.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }
        }

            holder.tvRequestDetails.setText(details.toString());
        }

        // 3. Xử lý Phê duyệt
        if (holder.btnApprove != null) {
            holder.btnApprove.setEnabled(true);
            holder.btnApprove.setOnClickListener(v -> {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;

                holder.btnApprove.setEnabled(false);

                // 1. Cập nhật role trong bảng users
                db.collection("users").document(request.getUserId())
                        .update("role", request.getRequestedRole())
                        .addOnSuccessListener(aVoid -> {
                            // 2. Đổi trạng thái đơn thành APPROVED
                            db.collection("role_requests").document(request.getRequestId())
                                    .update("status", "APPROVED")
                                    .addOnSuccessListener(aVoid2 -> {
                                        if (requestList != null && adapterPos < requestList.size()) {
                                            requestList.remove(adapterPos);
                                            notifyItemRemoved(adapterPos);
                                            notifyItemRangeChanged(adapterPos, requestList.size());
                                        }
                                        if (v.getContext() != null) {
                                            Toast.makeText(v.getContext(), "Đã cấp quyền " + request.getRequestedRole(), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        holder.btnApprove.setEnabled(true);
                                        if (v.getContext() != null) {
                                            Toast.makeText(v.getContext(), "Lỗi cập nhật trạng thái: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .addOnFailureListener(e -> {
                            holder.btnApprove.setEnabled(true);
                            if (v.getContext() != null) {
                                Toast.makeText(v.getContext(), "Lỗi phê duyệt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }

        // Xử lý Từ chối
        if (holder.btnReject != null) {
            holder.btnReject.setEnabled(true);
            holder.btnReject.setOnClickListener(v -> {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;

                holder.btnReject.setEnabled(false);

                db.collection("role_requests").document(request.getRequestId())
                        .update("status", "REJECTED")
                        .addOnSuccessListener(aVoid -> {
                            if (requestList != null && adapterPos < requestList.size()) {
                                requestList.remove(adapterPos);
                                notifyItemRemoved(adapterPos);
                                notifyItemRangeChanged(adapterPos, requestList.size());
                            }
                            if (v.getContext() != null) {
                                Toast.makeText(v.getContext(), "Đã từ chối yêu cầu", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            holder.btnReject.setEnabled(true);
                            if (v.getContext() != null) {
                                Toast.makeText(v.getContext(), "Lỗi từ chối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;    }

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