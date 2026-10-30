package com.group5.agrifoodshelper.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.SupportJob;
import java.util.ArrayList;
import java.util.List;

public class SupportJobAdapter extends RecyclerView.Adapter<SupportJobAdapter.JobViewHolder> {

    private List<SupportJob> jobList = new ArrayList<>();

    public void setJobList(List<SupportJob> list) {
        this.jobList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_support_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        if (jobList == null || position < 0 || position >= jobList.size()) {
            return;
        }
        SupportJob job = jobList.get(position);
        if (job == null) return;
        if (holder.tvJobTitle != null) {
            holder.tvJobTitle.setText(job.getTitle());
        }
        if (holder.tvFarmName != null) {
            holder.tvFarmName.setText("Chủ vườn: " + job.getFarmName());
        }
        if (holder.tvJobDate != null) {
            holder.tvJobDate.setText("Thời gian: " + job.getDate());
        }
        if (holder.tvJobAddress != null) {
            holder.tvJobAddress.setText("Địa chỉ: " + job.getAddress());
        }
        // Bắt sự kiện đăng ký tham gia
        if (holder.btnApplyJob != null) {
            holder.btnApplyJob.setEnabled(true);
            holder.btnApplyJob.setText("Đăng ký hỗ trợ");
            holder.btnApplyJob.setOnClickListener(v -> {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;
                // TODO: Lưu data người ứng tuyển vào Firestore sau (nếu cần)
                if (v.getContext() != null) {
                    Toast.makeText(v.getContext(), "Đã gửi yêu cầu đăng ký hỗ trợ!", Toast.LENGTH_SHORT).show();
                }
                holder.btnApplyJob.setText("Đang chờ duyệt");
                holder.btnApplyJob.setEnabled(false);
            });
        }
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvFarmName, tvJobDate, tvJobAddress;
        MaterialButton btnApplyJob;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvFarmName = itemView.findViewById(R.id.tvFarmName);
            tvJobDate = itemView.findViewById(R.id.tvJobDate);
            tvJobAddress = itemView.findViewById(R.id.tvJobAddress);
            btnApplyJob = itemView.findViewById(R.id.btnApplyJob);
        }
    }
}