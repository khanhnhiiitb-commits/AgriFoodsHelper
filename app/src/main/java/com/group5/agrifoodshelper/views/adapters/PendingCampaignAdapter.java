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
import com.group5.agrifoodshelper.models.FarmerReport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingCampaignAdapter extends RecyclerView.Adapter<PendingCampaignAdapter.PendingViewHolder> {

    private List<FarmerReport> pendingList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void setPendingList(List<FarmerReport> list) {
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
        FarmerReport report = pendingList.get(position);

        holder.tvPendingTitle.setText(report.getTitle());
        holder.tvPendingAuthor.setText("Sản phẩm: " + report.getProductName());
        holder.tvPendingAddress.setText("Địa điểm: " + (report.getLocationData() != null ? report.getLocationData() : "Chưa rõ"));
        holder.tvPendingQuantity.setText(String.format("Sản lượng cần cứu: %,.0f kg", report.getEstimatedQuantity()));

        holder.btnApproveCampaign.setOnClickListener(v -> {
            // Khóa nút để tránh bấm 2 lần
            holder.btnApproveCampaign.setEnabled(false);
            holder.btnApproveCampaign.setText("Đang xử lý...");
            updateReportStatus(report, "APPROVED", position, v);
        });

        holder.btnRejectCampaign.setOnClickListener(v -> {
            holder.btnRejectCampaign.setEnabled(false);
            updateReportStatus(report, "REJECTED", position, v);
        });
    }

    // Hàm gọi API Firebase để cập nhật trạng thái
    private void updateReportStatus(FarmerReport report, String newStatus, int position, View view) {
        db.collection("farmer_reports").document(report.getReportId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Xóa item khỏi danh sách chờ ngay lập tức cho mượt giao diện
                    pendingList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, pendingList.size());

                    if (newStatus.equals("APPROVED")) {
                        // CHUYỂN ĐỔI: Tự động đẩy sang Bảng điều phối (Campaigns)
                        createCampaignFromReport(report, view);
                    } else {
                        Toast.makeText(view.getContext(), "Đã từ chối yêu cầu.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(view.getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Tự động trích xuất dữ liệu để tạo Chiến dịch (Campaign)
    // Tự động trích xuất dữ liệu để tạo Chiến dịch (Campaign)
    private void createCampaignFromReport(FarmerReport report, View view) {
        // Lấy thông tin từ bảng farmer_reports trên Firestore để lấy đúng imageUrl trên Cloud
        db.collection("farmer_reports").document(report.getReportId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Lấy đường dẫn cloud URL thực tế mà SyncWorker đã upload lên Storage
                    String cloudImageUrl = documentSnapshot.getString("imageUrl");

                    Map<String, Object> newCampaign = new HashMap<>();
                    String newCampaignId = "CAMP_" + System.currentTimeMillis();

                    newCampaign.put("campaignId", newCampaignId);
                    newCampaign.put("authorId", report.getUserId()); // Để tab "Chiến dịch của tôi" lọc đúng chủ đơn
                    newCampaign.put("title", report.getTitle() != null ? report.getTitle() : "Giải cứu " + report.getProductName());
                    newCampaign.put("detail", report.getDetail() != null ? report.getDetail() : "Chung tay hỗ trợ nông dân vượt qua khó khăn.");
                    newCampaign.put("targetQuantity", report.getEstimatedQuantity());
                    newCampaign.put("currentQuantity", 0.0); // Mới tạo nên số lượng gom = 0
                    newCampaign.put("address", report.getLocationData());

                    // SỬ DỤNG CLOUD URL THAY CHO LOCAL URI
                    newCampaign.put("imageUrl", cloudImageUrl != null ? cloudImageUrl : "");

                    newCampaign.put("createdAt", System.currentTimeMillis());
                    newCampaign.put("isPinned", false); // Không ưu tiên khẩn cấp bằng Admin tạo
                    newCampaign.put("status", "ACTIVE");
                    newCampaign.put("authorName", "Nông dân địa phương");

                    // Ghi vào collection Campaigns để hiện lên Bảng điều phối cho Tài xế
                    db.collection("Campaigns").document(newCampaignId)
                            .set(newCampaign)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(view.getContext(), "Đã duyệt và tạo lệnh điều phối thành công!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(view.getContext(), "Lỗi tạo lệnh điều phối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(view.getContext(), "Không thể lấy thông tin ảnh từ báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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