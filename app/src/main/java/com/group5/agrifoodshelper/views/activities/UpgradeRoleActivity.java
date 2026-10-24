package com.group5.agrifoodshelper.views.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.R;
import java.util.HashMap;
import java.util.Map;

public class UpgradeRoleActivity extends AppCompatActivity {

    private RadioGroup rgRoleSelection;
    private LinearLayout layoutFarmerInfo, layoutDriverInfo;
    private TextInputEditText edtFarmName, edtFarmAddress, edtFarmArea;
    private TextInputEditText edtLicensePlate, edtVehicleType;
    private MaterialButton btnSubmitRequest;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String selectedRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_role);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Ánh xạ
        rgRoleSelection = findViewById(R.id.rgRoleSelection);
        layoutFarmerInfo = findViewById(R.id.layoutFarmerInfo);
        layoutDriverInfo = findViewById(R.id.layoutDriverInfo);

        edtFarmName = findViewById(R.id.edtFarmName);
        edtFarmAddress = findViewById(R.id.edtFarmAddress);
        edtFarmArea = findViewById(R.id.edtFarmArea);

        edtLicensePlate = findViewById(R.id.edtLicensePlate);
        edtVehicleType = findViewById(R.id.edtVehicleType);

        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        // Lắng nghe sự kiện chọn Radio Button để đổi Form
        rgRoleSelection.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbFarmer) {
                selectedRole = "FARMER";
                layoutFarmerInfo.setVisibility(View.VISIBLE);
                layoutDriverInfo.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbDriver) {
                selectedRole = "DRIVER";
                layoutFarmerInfo.setVisibility(View.GONE);
                layoutDriverInfo.setVisibility(View.VISIBLE);
            }
        });

        // Xử lý Gửi dữ liệu
        btnSubmitRequest.setOnClickListener(v -> submitUpgradeRequest());
    }

    private void submitUpgradeRequest() {
        if (selectedRole.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn vai trò bạn muốn đăng ký", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        // Tạo gói dữ liệu tổng hợp
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("userId", userId);
        requestData.put("requestedRole", selectedRole);
        requestData.put("status", "PENDING"); // Đặt trạng thái Chờ duyệt
        requestData.put("timestamp", System.currentTimeMillis());

        // Đóng gói thông tin chi tiết (Map con)
        Map<String, String> infoMap = new HashMap<>();

        if (selectedRole.equals("FARMER")) {
            String name = edtFarmName.getText().toString().trim();
            String address = edtFarmAddress.getText().toString().trim();
            String area = edtFarmArea.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và địa chỉ vườn", Toast.LENGTH_SHORT).show();
                return;
            }
            infoMap.put("farmName", name);
            infoMap.put("farmAddress", address);
            infoMap.put("farmArea", area);

        } else if (selectedRole.equals("DRIVER")) {
            String plate = edtLicensePlate.getText().toString().trim();
            String type = edtVehicleType.getText().toString().trim();

            if (plate.isEmpty() || type.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập biển số và loại xe", Toast.LENGTH_SHORT).show();
                return;
            }
            infoMap.put("licensePlate", plate);
            infoMap.put("vehicleType", type);
        }

        requestData.put("info", infoMap);

        // Khóa nút để tránh bấm 2 lần
        btnSubmitRequest.setEnabled(false);
        btnSubmitRequest.setText("Đang gửi...");

        // Đẩy lên Firestore bảng role_requests
        db.collection("role_requests").add(requestData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Gửi yêu cầu thành công! Vui lòng chờ duyệt.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitRequest.setEnabled(true);
                    btnSubmitRequest.setText("Gửi Yêu Cầu Duyệt");
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}