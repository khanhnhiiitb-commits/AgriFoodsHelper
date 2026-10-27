package com.group5.agrifoodshelper.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.repositories.JobApplicationRepository;

public class ManageApplicantsActivity extends AppCompatActivity {

    private JobApplicationRepository repository;

    // Lưu ý: Trong thực tế, 2 biến này sẽ được lấy từ Intent (getIntent().getStringExtra(...))
    // hoặc thông qua Adapter khi click vào 1 item ứng viên cụ thể.
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
                    tvApplicantStatus.setText("Trạng thái: TỪ CHỐI");
                    tvApplicantStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    Toast.makeText(ManageApplicantsActivity.this, "Đã từ chối ứng viên", Toast.LENGTH_SHORT).show();

                    // Ẩn 2 nút sau khi đã xử lý xong
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ManageApplicantsActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Xử lý nút DUYỆT
        btnApprove.setOnClickListener(v -> {
            repository.updateApplicationStatus(currentAppId, "APPROVED", new JobApplicationRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    tvApplicantStatus.setText("Trạng thái: ĐÃ DUYỆT");
                    tvApplicantStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    Toast.makeText(ManageApplicantsActivity.this, "Đã duyệt! Đang mở trình gọi điện...", Toast.LENGTH_SHORT).show();

                    // Ẩn 2 nút sau khi đã duyệt xong
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);

                    // Gọi Implicit Intent để mở bàn phím gọi điện
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + applicantPhone));

                    // Kiểm tra xem thiết bị có ứng dụng xử lý cuộc gọi hay không trước khi start Activity
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