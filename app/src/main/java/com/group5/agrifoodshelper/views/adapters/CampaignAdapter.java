package com.group5.agrifoodshelper.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Campaign;
import java.util.ArrayList;
import java.util.List;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.CampaignViewHolder> {

    private List<Campaign> campaignList = new ArrayList<>();
    private OnItemClickListener listener;
    private boolean isAdmin = false;

    public interface OnItemClickListener {
        void onItemClick(Campaign campaign);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setCampaignList(List<Campaign> list) {
        this.campaignList = list;
        notifyDataSetChanged();
    }

    public void setAdminStatus(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CampaignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_campaign_card, parent, false);
        return new CampaignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignViewHolder holder, int position) {
        Campaign campaign = campaignList.get(position);

        holder.tvTitle.setText(campaign.getTitle());

        double current = campaign.getCurrentQuantity();
        double target = campaign.getTargetQuantity();

        String progressText = String.format("Đã góp: %,.0f / %,.0f kg", current, target);
        holder.tvProgressText.setText(progressText);

        int percent = 0;
        if (target > 0) {
            percent = (int) ((current / target) * 100);
        }

        holder.tvPercentage.setText(percent + "%");
        holder.progressBar.setProgress(percent);

        String imageUrl = campaign.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
        }

        // 1. LOGIC ẨN/HIỆN NÚT ADMIN
        holder.layoutAdminControls.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        // 2. SỰ KIỆN XÓA BÀI
        holder.btnDeleteCampaign.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("Campaigns")
                    .document(campaign.getCampaignId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        campaignList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, campaignList.size());
                        Toast.makeText(v.getContext(), "Đã xóa bài đăng", Toast.LENGTH_SHORT).show();
                    });
        });
        // 3. LOGIC HIỂN THỊ VÀ SỰ KIỆN NÚT GHIM
        // Đổi màu icon nếu bài đang được ghim để Admin dễ nhận biết
        if (campaign.isPinned()) {
            holder.btnPinCampaign.setColorFilter(android.graphics.Color.parseColor("#E53935")); // Đổi màu đỏ
        } else {
            holder.btnPinCampaign.clearColorFilter(); // Về màu mặc định
        }

        holder.btnPinCampaign.setOnClickListener(v -> {
            boolean newPinStatus = !campaign.isPinned(); // Đảo ngược trạng thái
            holder.btnPinCampaign.setEnabled(false); // Khóa nút tạm thời tránh spam click

            FirebaseFirestore.getInstance().collection("Campaigns")
                    .document(campaign.getCampaignId())
                    .update("isPinned", newPinStatus)
                    .addOnSuccessListener(aVoid -> {
                        holder.btnPinCampaign.setEnabled(true);
                        campaign.setPinned(newPinStatus); // Cập nhật dữ liệu ngay tại chỗ
                        notifyItemChanged(position); // Báo Adapter vẽ lại thẻ này để đổi màu icon

                        String msg = newPinStatus ? "Đã ghim bài viết" : "Đã bỏ ghim";
                        Toast.makeText(v.getContext(), msg, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        holder.btnPinCampaign.setEnabled(true);
                        Toast.makeText(v.getContext(), "Lỗi ghim bài: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
        holder.btnEditCampaign.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), com.group5.agrifoodshelper.views.activities.EditCampaignActivity.class);
            intent.putExtra("CAMPAIGN_ID", campaign.getCampaignId());
            v.getContext().startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(campaign);
        });
    }

    @Override
    public int getItemCount() {
        return campaignList.size();
    }

    class CampaignViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvProgressText, tvPercentage;
        ProgressBar progressBar;
        ImageView imgProduct;

        // KHAI BÁO BIẾN GIAO DIỆN ADMIN
        LinearLayout layoutAdminControls;
        ImageButton btnEditCampaign, btnDeleteCampaign,btnPinCampaign;

        public CampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
            imgProduct = itemView.findViewById(R.id.imgProduct);

            // ÁNH XẠ GIAO DIỆN ADMIN TỪ XML
            layoutAdminControls = itemView.findViewById(R.id.layoutAdminControls);
            btnEditCampaign = itemView.findViewById(R.id.btnEditCampaign);
            btnDeleteCampaign = itemView.findViewById(R.id.btnDeleteCampaign);
            btnPinCampaign = itemView.findViewById(R.id.btnPinCampaign);
        }
    }
}