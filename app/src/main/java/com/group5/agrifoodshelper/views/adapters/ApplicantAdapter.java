package com.group5.agrifoodshelper.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.JobApplication;
import java.util.ArrayList;
import java.util.List;

public class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.ApplicantViewHolder> {

    private List<JobApplication> applicantList = new ArrayList<>();
    private Context context;
    private FirebaseFirestore db;

    public ApplicantAdapter(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setApplicantList(List<JobApplication> list) {
        this.applicantList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ApplicantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Lưu ý: Bạn cần tạo một file XML giao diện tên là item_applicant.xml cho mỗi dòng
        View view = LayoutInflater.from(context).inflate(R.layout.item_applicant, parent, false);
        return new ApplicantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicantViewHolder holder, int position) {
        JobApplication app = applicantList.get(position);

        // Ánh xạ dữ liệu lên UI (Bạn có thể tùy chỉnh thêm dựa vào XML của bạn)
        holder.tvRole.setText("Vai trò: " + app.getRole());
        holder.tvVehicle.setText("Xe: " + (app.getVehicleInfo() != null ? app.getVehicleInfo() : "Không có"));
        holder.tvStatus.setText("Trạng thái: " + app.getStatus());

        // Nếu trạng thái không phải PENDING (nghĩa là đã duyệt hoặc từ chối rồi) -> Ẩn 2 nút đi
        if (!"PENDING".equals(app.getStatus())) {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        } else {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        }

        // XỬ LÝ NÚT DUYỆT
        holder.btnApprove.setOnClickListener(v -> {
            holder.btnApprove.setEnabled(false); // Khóa nút tạm thời

            db.collection("job_applications").document(app.getApplicationId())
                    .update("status", "APPROVED")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Đã duyệt! Đang mở trình gọi điện...", Toast.LENGTH_SHORT).show();

                        // Cập nhật lại UI dòng này
                        app.setStatus("APPROVED");
                        notifyItemChanged(position);

                        // Mở trình gọi điện
                        if (app.getPhoneNumber() != null && !app.getPhoneNumber().isEmpty()) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + app.getPhoneNumber()));
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Ứng viên này chưa cập nhật số điện thoại", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        holder.btnApprove.setEnabled(true);
                    });
        });

        // XỬ LÝ NÚT TỪ CHỐI
        holder.btnReject.setOnClickListener(v -> {
            holder.btnReject.setEnabled(false);

            db.collection("job_applications").document(app.getApplicationId())
                    .update("status", "REJECTED")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Đã từ chối ứng viên", Toast.LENGTH_SHORT).show();
                        // Cập nhật lại UI
                        app.setStatus("REJECTED");
                        notifyItemChanged(position);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        holder.btnReject.setEnabled(true);
                    });
        });
    }

    @Override
    public int getItemCount() {
        return applicantList != null ? applicantList.size() : 0;
    }

    // Khai báo các view trong item_applicant.xml
    static class ApplicantViewHolder extends RecyclerView.ViewHolder {
        TextView tvRole, tvVehicle, tvStatus;
        Button btnApprove, btnReject;

        public ApplicantViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bạn nhớ đặt ID trong file item_applicant.xml khớp với các ID này
            tvRole = itemView.findViewById(R.id.tvApplicantRole);
            tvVehicle = itemView.findViewById(R.id.tvApplicantVehicle);
            tvStatus = itemView.findViewById(R.id.tvApplicantStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}