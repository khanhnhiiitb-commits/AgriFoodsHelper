package com.group5.agrifoodshelper.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Campaign;
import com.group5.agrifoodshelper.models.Mobilization;
import com.group5.agrifoodshelper.viewmodels.JobBoardViewModel;

import java.util.ArrayList;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    // SỬA KIỂU DỮ LIỆU TỪ Campaign SANG JobBoardViewModel.JobBoardItem
    private List<JobBoardViewModel.JobBoardItem> jobList = new ArrayList<>();

    public void setJobList(List<JobBoardViewModel.JobBoardItem> jobs) {
        this.jobList = jobs != null ? jobs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_card, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        if (jobList == null || position < 0 || position >= jobList.size()) {
            return;
        }
        JobBoardViewModel.JobBoardItem item = jobList.get(position);
        if (item == null) return;
        Campaign campaign = item.getCampaign();
        Mobilization mobilization = item.getMobilization();
        if (campaign == null) return;
        // Lấy số liệu gốc từ Model
        double current = campaign.getCurrentQuantity();
        double target = campaign.getTargetQuantity();

        // 1. Ánh xạ thông tin cơ bản
        if (holder.tvJobTitle != null) {
            holder.tvJobTitle.setText(campaign.getTitle());
        }
        if (holder.tvJobProgressSummary != null) {
            holder.tvJobProgressSummary.setText(String.format("Đã gom: %,.0f / %,.0f kg", current, target));
        }
        // Cờ khẩn cấp
        if (holder.tvJobUrgentBadge != null) {
            holder.tvJobUrgentBadge.setVisibility(campaign.isPinned() ? View.VISIBLE : View.GONE);
        }
        // 2. Ánh xạ địa điểm & Thời gian
        if (holder.tvJobPickup != null) {
            holder.tvJobPickup.setText(campaign.getAddress() != null ? campaign.getAddress() : "Đang cập nhật");
        }
        if (holder.tvJobDropoff != null) {
            holder.tvJobDropoff.setText("TP. Hồ Chí Minh"); // Fix cứng tạm thời theo thiết kế
        }
        if (holder.tvJobTime != null) {
            holder.tvJobTime.setText("Hôm nay - Trước 16:00");
        }
        // 3. Ánh xạ Khối lượng & Tiến độ
        if (holder.tvJobRequiredWeight != null) {
            holder.tvJobRequiredWeight.setText(String.format("%,.0f kg", target));
        }
        if (holder.tvJobReceivedTrucks != null) {
            holder.tvJobReceivedTrucks.setText(String.format("Đã nhận %,.0f kg", current));
        }
        // Tự tính phần trăm tiến độ
        int percent = 0;
        if (target > 0) {
            percent = (int) ((current / target) * 100);
        }
        if (holder.progressBarJobTrucks != null) {
            holder.progressBarJobTrucks.setProgress(percent);
        }
        // 4. Bắt sự kiện bấm nút Nhận chuyến
        if (holder.btnAcceptJob != null) {
            holder.btnAcceptJob.setOnClickListener(v -> {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;

                // Lấy lại campaign chính xác từ vị trí hiện tại của Adapter
                JobBoardViewModel.JobBoardItem currentItem = jobList.get(adapterPos);
                if (currentItem == null || currentItem.getCampaign() == null) return;
                Campaign currentCampaign = currentItem.getCampaign();
                Mobilization currentMobilization = currentItem.getMobilization();
                // Khởi tạo Form Bottom Sheet
                com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                        new com.google.android.material.bottomsheet.BottomSheetDialog(v.getContext());

                View sheetView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_accept_job, null);
                bottomSheetDialog.setContentView(sheetView);

                // Ánh xạ các view trong Form
                TextView tvDialogCampaignName = sheetView.findViewById(R.id.tvDialogCampaignName);
                com.google.android.material.button.MaterialButton btnCancelDialog = sheetView.findViewById(R.id.btnCancelDialog);
                com.google.android.material.button.MaterialButton btnConfirmJob = sheetView.findViewById(R.id.btnConfirmJob);
                com.google.android.material.textfield.TextInputEditText edtVehicleInfo = sheetView.findViewById(R.id.edtVehicleInfo);
                com.google.android.material.textfield.TextInputEditText edtEta = sheetView.findViewById(R.id.edtEta);

                // Đổ tên chiến dịch vào Form
                if (tvDialogCampaignName != null) {
                    tvDialogCampaignName.setText("Chiến dịch: " + (currentCampaign.getTitle() != null ? currentCampaign.getTitle() : ""));
                }

                // Xử lý nút Hủy
                if (btnCancelDialog != null) {
                    btnCancelDialog.setOnClickListener(cancelView -> bottomSheetDialog.dismiss());
                }

                // Xử lý nút Xác nhận
                if (btnConfirmJob != null) {
                    btnConfirmJob.setOnClickListener(confirmView -> {
                        if (edtVehicleInfo == null || edtEta == null) return;
                        String vehicle = edtVehicleInfo.getText().toString().trim();
                        String eta = edtEta.getText().toString().trim();

                        if (vehicle.isEmpty()) {
                            edtVehicleInfo.setError("Vui lòng nhập thông tin xe");
                            return;
                        }

                        // Vô hiệu hóa nút bấm để tránh người dùng nhấn 2 lần
                        btnConfirmJob.setEnabled(false);
                        btnConfirmJob.setText("Đang xử lý...");

                        // Khởi tạo kết nối Firebase
                        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();

                        // Lấy ID người dùng đang đăng nhập (Tài xế)
                        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "UNKNOWN_DRIVER";

                        // ======== SỬA LẠI THEO ĐÚNG YÊU CẦU FR09 ========
                        // 1. Tạo document mới trong collection "job_applications"
                        com.google.firebase.firestore.DocumentReference newAppRef = db.collection("job_applications").document();

                        // 2. Gói thông tin Ứng tuyển (Chờ duyệt)
                        java.util.Map<String, Object> jobRecord = new java.util.HashMap<>();
                        jobRecord.put("applicationId", newAppRef.getId()); // Lưu luôn ID của đơn
                        jobRecord.put("jobId", currentCampaign.getCampaignId());
                        if (currentMobilization != null) {
                            jobRecord.put("mobilizationId", currentMobilization.getMobilizationId());
                        }
                        jobRecord.put("userId", currentUserId);
                        jobRecord.put("role", "Tài xế");
                        jobRecord.put("status", "PENDING"); // QUAN TRỌNG: Status mặc định khi vừa nộp là PENDING
                        // Giữ lại 2 thông tin phụ bạn làm thêm ở bottom sheet
                        jobRecord.put("vehicleInfo", vehicle);
                        jobRecord.put("eta", eta);
                        jobRecord.put("appliedAt", System.currentTimeMillis());

                        // 3. Đẩy lên collection "job_applications"
                        newAppRef.set(jobRecord)
                                .addOnSuccessListener(aVoid -> {
                                    if (v.getContext() != null) {
                                        android.widget.Toast.makeText(v.getContext(), "Đã gửi yêu cầu! Vui lòng đợi Nông dân duyệt.", android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                    if (holder.btnAcceptJob != null) {
                                        holder.btnAcceptJob.setText("Đang chờ duyệt");
                                        holder.btnAcceptJob.setEnabled(false); // Khóa nút lại không cho bấm nữa
                                    }
                                    bottomSheetDialog.dismiss(); // Đóng form
                                })
                                .addOnFailureListener(e -> {
                                    if (v.getContext() != null) {
                                        android.widget.Toast.makeText(v.getContext(), "Lỗi đường truyền: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                    btnConfirmJob.setEnabled(true);
                                    btnConfirmJob.setText("Xác nhận");
                                });

                    });
                }
                // Hiển thị Form lên màn hình
                bottomSheetDialog.show();
            });
        }
    }
    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    // Class ViewHolder để tìm các ID trong file XML
    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvJobProgressSummary, tvJobUrgentBadge;
        TextView tvJobPickup, tvJobDropoff, tvJobTime;
        TextView tvJobRequiredWeight, tvJobReceivedTrucks;
        ProgressBar progressBarJobTrucks;
        MaterialButton btnAcceptJob;
        ImageView imgJobThumbnail;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvJobProgressSummary = itemView.findViewById(R.id.tvJobProgressSummary);
            tvJobUrgentBadge = itemView.findViewById(R.id.tvJobUrgentBadge);

            tvJobPickup = itemView.findViewById(R.id.tvJobPickup);
            tvJobDropoff = itemView.findViewById(R.id.tvJobDropoff);
            tvJobTime = itemView.findViewById(R.id.tvJobTime);

            tvJobRequiredWeight = itemView.findViewById(R.id.tvJobRequiredWeight);
            tvJobReceivedTrucks = itemView.findViewById(R.id.tvJobReceivedTrucks);
            progressBarJobTrucks = itemView.findViewById(R.id.progressBarJobTrucks);

            btnAcceptJob = itemView.findViewById(R.id.btnAcceptJob);
            imgJobThumbnail = itemView.findViewById(R.id.imgJobThumbnail);
        }
    }
}