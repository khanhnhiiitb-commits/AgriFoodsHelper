package com.group5.agrifoodshelper.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Contribution;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContributionAdapter extends RecyclerView.Adapter<ContributionAdapter.ViewHolder> {

    // Gói một lượt góp kèm tên chiến dịch để hiển thị
    public static class Item {
        private final Contribution contribution;
        private final String campaignTitle;

        public Item(Contribution contribution, String campaignTitle) {
            this.contribution = contribution;
            this.campaignTitle = campaignTitle;
        }

        public Contribution getContribution() { return contribution; }
        public String getCampaignTitle() { return campaignTitle; }
    }

    private List<Item> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public void setItems(List<Item> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contribution, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (items == null || position < 0 || position >= items.size()) {
            return;
        }
        Item item = items.get(position);
        if (item == null) return;
        Contribution c = item.getContribution();
        if (c == null) return;
        if (holder.tvTitle != null) {
        String title = item.getCampaignTitle() != null && !item.getCampaignTitle().isEmpty()
                ? item.getCampaignTitle() : "Chiến dịch không còn tồn tại";
            holder.tvTitle.setText(title);
        }
        if (holder.tvQuantity != null) {
            holder.tvQuantity.setText(String.format(Locale.getDefault(), "Đã góp: %,.0f kg", c.getQuantityKg()));
        }
        if (holder.tvTotal != null) {
            holder.tvTotal.setText(String.format(Locale.getDefault(), "Tổng tiền: %,.0f đ", c.getTotalPrice()));
        }
        if (holder.tvTime != null) {
            holder.tvTime.setText("Thời gian: " + dateFormat.format(new Date(c.getTimestamp())));
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvQuantity, tvTotal, tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvContributionTitle);
            tvQuantity = itemView.findViewById(R.id.tvContributionQuantity);
            tvTotal = itemView.findViewById(R.id.tvContributionTotal);
            tvTime = itemView.findViewById(R.id.tvContributionTime);
        }
    }
}