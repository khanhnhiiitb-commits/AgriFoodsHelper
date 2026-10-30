package com.group5.agrifoodshelper.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Campaign;
import com.group5.agrifoodshelper.models.Mobilization;
import com.group5.agrifoodshelper.viewmodels.JobBoardViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AcceptedJobAdapter extends RecyclerView.Adapter<AcceptedJobAdapter.ViewHolder> {

    private List<JobBoardViewModel.JobBoardItem> items = new ArrayList<>();

    public void setItems(List<JobBoardViewModel.JobBoardItem> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accepted_job, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (items == null || position < 0 || position >= items.size()) {
            return;
        }
        JobBoardViewModel.JobBoardItem item = items.get(position);
        if (item == null) return;

        Mobilization mobilization = item.getMobilization();
        Campaign campaign = item.getCampaign();
        if (holder.tvTitle != null) {
        String title = (campaign != null && campaign.getTitle() != null)
                ? campaign.getTitle() : "Chuyến xe";
        holder.tvTitle.setText(title);
        }
        if (holder.tvAddress != null) {
        String rawAddress = campaign != null ? campaign.getAddress() : null;
        holder.tvAddress.setText("Địa điểm: " + parseAddress(rawAddress));
        }
        if (holder.tvCapacity != null && mobilization != null) {
        if ("TRUCK".equals(mobilization.getType())) {
            holder.tvCapacity.setText("Trọng tải yêu cầu: " + mobilization.getRequiredCount());
        } else {
            holder.tvCapacity.setText("Số lượng yêu cầu: " + mobilization.getRequiredCount());
        }
        }
        if (holder.tvStatus != null && mobilization != null) {
        holder.tvStatus.setText(statusLabel(mobilization.getStatus()));
        }
    }

    private String parseAddress(String raw) {
        if (raw == null || raw.isEmpty()) return "Chưa rõ";
        try {
            return new JSONObject(raw).getString("address");
        } catch (JSONException e) {
            return raw;
        }
    }

    private String statusLabel(String status) {
        if ("CONFIRMED".equals(status)) return "Đã nhận, chờ vận chuyển";
        if ("COMPLETED".equals(status)) return "Đã hoàn thành";
        return status != null ? status : "";
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAddress, tvCapacity, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAcceptedTitle);
            tvAddress = itemView.findViewById(R.id.tvAcceptedAddress);
            tvCapacity = itemView.findViewById(R.id.tvAcceptedCapacity);
            tvStatus = itemView.findViewById(R.id.tvAcceptedStatus);
        }
    }
}