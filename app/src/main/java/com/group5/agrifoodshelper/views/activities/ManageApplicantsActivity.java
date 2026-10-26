package com.group5.agrifoodshelper.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.repositories.JobApplicationRepository;

public class ManageApplicantsActivity extends AppCompatActivity {

    private JobApplicationRepository repository;

    private String currentAppId = "APP_12345";
    private String applicantPhone = "0901234567";

    private TextView tvApplicantStatus;
    private Button btnApprove, btnReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_applicants);

        repository = new JobApplicationRepository();

        tvApplicantStatus = findViewById(R.id.tvApplicantStatus);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);

        // Xử lý nút TỪ CHỐI
        btnReject.setOnClickListener(v -> {
            repository.updateApplicationStatus(currentAppId, "REJECTED", new JobApplicationRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    tvApplicantStatus.setText("Trạng thái: REJECTED");
                    tvApplicantStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    Toast.makeText(ManageApplicantsActivity.this, "Đã từ chối ứng viên", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ManageApplicantsActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnApprove.setOnClickListener(v -> {
            repository.updateApplicationStatus(currentAppId, "APPROVED", new JobApplicationRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    tvApplicantStatus.setText("Trạng thái: APPROVED");
                    tvApplicantStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    Toast.makeText(ManageApplicantsActivity.this, "Đã duyệt! Đang mở trình gọi điện...", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + applicantPhone));

                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(ManageApplicantsActivity.this, "Không tìm thấy ứng dụng gọi điện!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ManageApplicantsActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}