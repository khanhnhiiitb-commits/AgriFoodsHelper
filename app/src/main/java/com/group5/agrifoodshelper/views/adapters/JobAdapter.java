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
import java.util.ArrayList;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Campaign> jobList = new ArrayList<>();

    // Hàm cập nhật danh sách công việc khi Firebase trả dữ liệu về
    public void setJobList(List<Campaign> jobs) {
        this.jobList = jobs;
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
        Campaign campaign = jobList.get(position);

        // 1. Ánh xạ thông tin cơ bản
        holder.tvJobTitle.setText(campaign.getTitle());
        holder.tvJobProgressSummary.setText(campaign.getProgressSummary());

        // Cờ khẩn cấp
        holder.tvJobUrgentBadge.setVisibility(campaign.isUrgent() ? View.VISIBLE : View.GONE);

        // 2. Ánh xạ địa điểm & Thời gian
        // Tạm dùng getAddress() cho điểm bốc hàng, các điểm khác có thể bổ sung vào model sau
        holder.tvJobPickup.setText(campaign.getAddress() != null ? campaign.getAddress() : "Đang cập nhật");
        holder.tvJobDropoff.setText("TP. Hồ Chí Minh"); // Fix cứng tạm thời theo thiết kế
        holder.tvJobTime.setText("Hôm nay - Trước 16:00");

        // 3. Ánh xạ Khối lượng & Tiến độ
        holder.tvJobRequiredWeight.setText(String.format("%,.0f kg", campaign.getTargetQuantity()));
        holder.tvJobReceivedTrucks.setText(String.format("Đã nhận %,.0f kg", campaign.getCurrentQuantity()));
        holder.progressBarJobTrucks.setProgress(campaign.getProgressPercentage());

        // 4. Bắt sự kiện bấm nút Nhận chuyến
        // 4. Bắt sự kiện bấm nút Nhận chuyến
        holder.btnAcceptJob.setOnClickListener(v -> {
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
            tvDialogCampaignName.setText("Chiến dịch: " + campaign.getTitle());

            // Xử lý nút Hủy
            btnCancelDialog.setOnClickListener(cancelView -> bottomSheetDialog.dismiss());

            // Xử lý nút Xác nhận
            btnConfirmJob.setOnClickListener(confirmView -> {
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

                // 1. Gói thông tin Nhận chuyến (Transaction/Order)
                java.util.Map<String, Object> jobRecord = new java.util.HashMap<>();
                jobRecord.put("campaignId", campaign.getCampaignId());
                jobRecord.put("driverId", currentUserId);
                jobRecord.put("vehicleInfo", vehicle);
                jobRecord.put("eta", eta);
                jobRecord.put("acceptedAt", System.currentTimeMillis());
                jobRecord.put("status", "ACCEPTED"); // Trạng thái: Đã nhận, Đang di chuyển, Hoàn thành...

                // 2. Lưu thông tin này vào bộ sưu tập con "AcceptedJobs" nằm bên trong Chiến dịch này
                db.collection("Campaigns").document(campaign.getCampaignId())
                        .collection("AcceptedJobs")
                        .add(jobRecord)
                        .addOnSuccessListener(documentReference -> {
                            // 3. Cập nhật tiến độ của chiến dịch (Ví dụ: mỗi chiếc xe nhận lệnh sẽ cộng thêm 500kg vào tiến độ)
                            // Lưu ý: Tùy vào logic app của bạn mà con số 500 này có thể lấy từ input hoặc cố định
                            db.collection("Campaigns").document(campaign.getCampaignId())
                                    .update("currentQuantity", com.google.firebase.firestore.FieldValue.increment(500))
                                    .addOnSuccessListener(aVoid -> {
                                        android.widget.Toast.makeText(v.getContext(), "Đã nhận lệnh điều phối thành công!", android.widget.Toast.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            android.widget.Toast.makeText(v.getContext(), "Lỗi đường truyền: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                            btnConfirmJob.setEnabled(true);
                            btnConfirmJob.setText("Xác nhận");
                        });
            });

            // Hiển thị Form lên màn hình
            bottomSheetDialog.show();
        });
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