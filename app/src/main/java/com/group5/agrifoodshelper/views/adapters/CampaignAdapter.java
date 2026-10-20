package com.group5.agrifoodshelper.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Campaign;
import java.util.ArrayList;
import java.util.List;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.CampaignViewHolder> {

    private List<Campaign> campaignList = new ArrayList<>();
    private OnItemClickListener listener;

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

        String progressText = "Đã góp: " + campaign.getCurrentQuantity() + " / " + campaign.getTargetQuantity() + " kg";
        holder.tvProgressText.setText(progressText);

        int percent = campaign.getProgressPercentage();
        holder.tvPercentage.setText(percent + "%");
        holder.progressBar.setProgress(percent);

        String imageUrl = campaign.getImageUrl(); // Đảm bảo model Campaign có hàm getImageUrl()
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
        }

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

        public CampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
            imgProduct = itemView.findViewById(R.id.imgProduct);
        }
    }

}