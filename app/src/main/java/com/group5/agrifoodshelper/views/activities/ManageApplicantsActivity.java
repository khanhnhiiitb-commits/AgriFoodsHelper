package com.group5.agrifoodshelper.views.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.JobApplication;
import com.group5.agrifoodshelper.views.adapters.ApplicantAdapter;
import java.util.ArrayList;
import java.util.List;

public class ManageApplicantsActivity extends AppCompatActivity {

    private RecyclerView rcvApplicants;
    private ProgressBar progressBar;
    private ApplicantAdapter applicantAdapter;
    private FirebaseFirestore db;
    private String currentJobId; // ID rẫy/chiến dịch của Nông dân

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lưu ý: Cần có file activity_manage_applicants.xml chứa 1 RecyclerView và 1 ProgressBar
        setContentView(R.layout.activity_manage_applicants);

        // Lấy ID công việc hiện tại (Nông dân ấn vào chiến dịch nào thì truyền ID chiến dịch đó sang đây)
        currentJobId = getIntent().getStringExtra("JOB_ID");

        // Hardcode tạm thời nếu bạn test thử, xóa dòng này khi ghép luồng thực tế
        if(currentJobId == null) currentJobId = "TEST_JOB_ID";

        db = FirebaseFirestore.getInstance();

        rcvApplicants = findViewById(R.id.rcvApplicants);
        progressBar = findViewById(R.id.progressBar);

        // Cài đặt RecyclerView
        rcvApplicants.setLayoutManager(new LinearLayoutManager(this));
        applicantAdapter = new ApplicantAdapter(this);
        rcvApplicants.setAdapter(applicantAdapter);

        // Gọi hàm tải danh sách ứng viên
        loadApplicants();
    }

    private void loadApplicants() {
        progressBar.setVisibility(View.VISIBLE);

        // Truy vấn Firestore: Lấy tất cả đơn ứng tuyển thuộc về jobId này
        db.collection("job_applications")
                .whereEqualTo("jobId", currentJobId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<JobApplication> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        JobApplication app = doc.toObject(JobApplication.class);
                        list.add(app);
                    }

                    if (list.isEmpty()) {
                        Toast.makeText(this, "Chưa có ai ứng tuyển!", Toast.LENGTH_SHORT).show();
                    }

                    // Cập nhật dữ liệu lên UI
                    applicantAdapter.setApplicantList(list);
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}