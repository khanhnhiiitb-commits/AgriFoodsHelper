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
import com.group5.agrifoodshelper.models.Campaign;
import java.util.ArrayList;
import java.util.List;

public class PendingCampaignAdapter extends RecyclerView.Adapter<PendingCampaignAdapter.PendingViewHolder> {

    private List<Campaign> pendingList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void setPendingList(List<Campaign> list) {
        this.pendingList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_campaign, parent, false);
        return new PendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingViewHolder holder, int position) {
        Campaign campaign = pendingList.get(position);

        // Ánh xạ dữ liệu lên giao diện
        holder.tvPendingTitle.setText(campaign.getTitle());
        holder.tvPendingAuthor.setText("Người đăng: " + (campaign.getAuthorName() != null ? campaign.getAuthorName() : "Khách"));
        holder.tvPendingAddress.setText("Địa điểm: " + (campaign.getAddress() != null ? campaign.getAddress() : "Chưa rõ"));
        holder.tvPendingQuantity.setText(String.format("Sản lượng cần cứu: %,.0f kg", campaign.getTargetQuantity()));

        // Xử lý nút PHÊ DUYỆT (Đổi status thành ACTIVE)
        holder.btnApproveCampaign.setOnClickListener(v -> {
            updateCampaignStatus(campaign, "ACTIVE", position, v);
        });

        // Xử lý nút TỪ CHỐI (Đổi status thành REJECTED)
        holder.btnRejectCampaign.setOnClickListener(v -> {
            updateCampaignStatus(campaign, "REJECTED", position, v);
        });
    }

    // Hàm gọi API Firebase để cập nhật trạng thái
    private void updateCampaignStatus(Campaign campaign, String newStatus, int position, View view) {
        db.collection("Campaigns").document(campaign.getCampaignId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    String msg = newStatus.equals("ACTIVE") ? "Đã duyệt thành công!" : "Đã từ chối chiến dịch.";
                    Toast.makeText(view.getContext(), msg, Toast.LENGTH_SHORT).show();

                    // Xóa item khỏi danh sách chờ hiển thị trên màn hình
                    pendingList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, pendingList.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(view.getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return pendingList != null ? pendingList.size() : 0;
    }

    static class PendingViewHolder extends RecyclerView.ViewHolder {
        TextView tvPendingTitle, tvPendingAuthor, tvPendingTime, tvPendingAddress, tvPendingQuantity;
        MaterialButton btnApproveCampaign, btnRejectCampaign;

        public PendingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPendingTitle = itemView.findViewById(R.id.tvPendingTitle);
            tvPendingAuthor = itemView.findViewById(R.id.tvPendingAuthor);
            tvPendingTime = itemView.findViewById(R.id.tvPendingTime);
            tvPendingAddress = itemView.findViewById(R.id.tvPendingAddress);
            tvPendingQuantity = itemView.findViewById(R.id.tvPendingQuantity);
            btnApproveCampaign = itemView.findViewById(R.id.btnApproveCampaign);
            btnRejectCampaign = itemView.findViewById(R.id.btnRejectCampaign);
        }
    }
}