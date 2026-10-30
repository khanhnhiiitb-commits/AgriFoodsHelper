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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Campaign;
import java.util.ArrayList;
import java.util.List;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.CampaignViewHolder> {

    private List<Campaign> campaignList = new ArrayList<>();
    private OnItemClickListener listener;
    private boolean isAdmin = false;

    public interface OnCampaignActionListener {
        void onCampaignAction();
    }
    private OnCampaignActionListener actionListener;

    public void setOnCampaignActionListener(OnCampaignActionListener listener) {
        this.actionListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Campaign campaign);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setCampaignList(List<Campaign> list) {
        this.campaignList = list != null ? list : new ArrayList<>();
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
        // 1. CHỐNG CRASH KHI XOAY MÀN HÌNH HOẶC DANH SÁCH TRỐNG
        if (campaignList == null || position < 0 || position >= campaignList.size()) {
            return;
        }

        Campaign campaign = campaignList.get(position);
        if (campaign == null) return;

        // 2. GÁN DỮ LIỆU AN TOÀN CHO CÁC VIEW
        if (holder.tvTitle != null) {
            holder.tvTitle.setText(campaign.getTitle() != null ? campaign.getTitle() : "");
        }

        double current = campaign.getCurrentQuantity();
        double target = campaign.getTargetQuantity();

        if (holder.tvProgressText != null) {
            String progressText = String.format("Đã góp: %,.0f / %,.0f kg", current, target);
            holder.tvProgressText.setText(progressText);
        }

        int percent = 0;
        if (target > 0) {
            percent = (int) ((current / target) * 100);
        }

        if (holder.tvPercentage != null) {
            holder.tvPercentage.setText(percent + "%");
        }
        if (holder.progressBar != null) {
            holder.progressBar.setProgress(percent);
        }

        if (holder.tvPrice != null) {
            String priceFormatted = String.format("%,.0f VNĐ", campaign.getPrice());
            holder.tvPrice.setText(priceFormatted);
        }

        String imageUrl = campaign.getImageUrl();
        if (holder.imgProduct != null) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(holder.imgProduct);
            } else {
                holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
            }
        }

        // 3. LOGIC ẨN/HIỆN NÚT ADMIN
        if (holder.layoutAdminControls != null) {
            holder.layoutAdminControls.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        // 4. SỰ KIỆN XÓA BÀI (Dùng holder.getAdapterPosition() để tránh lệch vị trí khi xoay)
        if (holder.btnDeleteCampaign != null) {
            holder.btnDeleteCampaign.setOnClickListener(v -> {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;

                FirebaseFirestore.getInstance().collection("Campaigns")
                        .document(campaign.getCampaignId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            if (adapterPos < campaignList.size()) {
                                campaignList.remove(adapterPos);
                                notifyItemRemoved(adapterPos);
                                notifyItemRangeChanged(adapterPos, campaignList.size());
                                Toast.makeText(v.getContext(), "Đã xóa bài đăng", Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }

        // 5. HIỂN THỊ CHỮ TRÊN NÚT GHIM
        if (holder.btnPinCampaign != null) {
            if (campaign.isPinned()) {
                holder.btnPinCampaign.setText("Bỏ ghim");
            } else {
                holder.btnPinCampaign.setText("Ghim bài");
            }

            // SỰ KIỆN GHIM BÀI
            holder.btnPinCampaign.setOnClickListener(v -> {
                boolean newPinStatus = !campaign.isPinned();
                holder.btnPinCampaign.setEnabled(false);

                FirebaseFirestore.getInstance().collection("Campaigns")
                        .document(campaign.getCampaignId())
                        .update("isPinned", newPinStatus)
                        .addOnSuccessListener(aVoid -> {
                            holder.btnPinCampaign.setEnabled(true);
                            campaign.setPinned(newPinStatus);

                            String msg = newPinStatus ? "Đã ghim bài viết" : "Đã bỏ ghim";
                            Toast.makeText(v.getContext(), msg, Toast.LENGTH_SHORT).show();

                            if (actionListener != null) {
                                actionListener.onCampaignAction();
                            }
                        })
                        .addOnFailureListener(e -> {
                            holder.btnPinCampaign.setEnabled(true);
                            Toast.makeText(v.getContext(), "Lỗi ghim bài", Toast.LENGTH_SHORT).show();
                        });
            });
        }

        if (holder.btnEditCampaign != null) {
            holder.btnEditCampaign.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), com.group5.agrifoodshelper.views.activities.EditCampaignActivity.class);
                intent.putExtra("CAMPAIGN_ID", campaign.getCampaignId());
                v.getContext().startActivity(intent);
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(campaign);
        });
    }

    @Override
    public int getItemCount() {
        return campaignList != null ? campaignList.size() : 0;
    }

    class CampaignViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvProgressText, tvPercentage, tvPrice;
        ProgressBar progressBar;
        ImageView imgProduct;
        MaterialButton btnPinCampaign;
        LinearLayout layoutAdminControls;
        ImageButton btnEditCampaign, btnDeleteCampaign;

        public CampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvPrice = itemView.findViewById(R.id.tvDetailPrice);
            layoutAdminControls = itemView.findViewById(R.id.layoutAdminControls);
            btnEditCampaign = itemView.findViewById(R.id.btnEditCampaign);
            btnDeleteCampaign = itemView.findViewById(R.id.btnDeleteCampaign);
            btnPinCampaign = itemView.findViewById(R.id.btnPinCampaign);
        }
    }
}