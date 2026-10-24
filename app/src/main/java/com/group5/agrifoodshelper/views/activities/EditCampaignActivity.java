package com.group5.agrifoodshelper.views.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.R;

public class EditCampaignActivity extends AppCompatActivity {

    // Thêm các biến mới
    private TextInputEditText edtEditTitle, edtEditTargetQuantity, edtEditAddress, edtEditDetail, edtEditPrice;
    private MaterialButton btnSaveEdit;
    private FirebaseFirestore db;
    private String campaignId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_campaign);

        edtEditTitle = findViewById(R.id.edtEditTitle);
        edtEditTargetQuantity = findViewById(R.id.edtEditTargetQuantity);
        edtEditAddress = findViewById(R.id.edtEditAddress); // Ánh xạ thêm
        edtEditDetail = findViewById(R.id.edtEditDetail);   // Ánh xạ thêm
        edtEditPrice = findViewById(R.id.edtEditPrice);     // Ánh xạ thêm
        btnSaveEdit = findViewById(R.id.btnSaveEdit);
        db = FirebaseFirestore.getInstance();

        campaignId = getIntent().getStringExtra("CAMPAIGN_ID");
        if (campaignId == null) {
            finish();
            return;
        }

        loadCampaignData();
        btnSaveEdit.setOnClickListener(v -> saveCampaignData());
    }

    private void loadCampaignData() {
        db.collection("Campaigns").document(campaignId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Tải dữ liệu cũ lên giao diện
                        if (doc.getString("title") != null) edtEditTitle.setText(doc.getString("title"));
                        if (doc.getDouble("targetQuantity") != null) edtEditTargetQuantity.setText(String.valueOf(doc.getDouble("targetQuantity")));
                        if (doc.getString("detail") != null) edtEditDetail.setText(doc.getString("detail"));
                        if (doc.getDouble("price") != null) edtEditPrice.setText(String.valueOf(doc.getDouble("price")));

                        // Bóc tách JSON địa chỉ để đưa lên ô nhập
                        String locStr = doc.getString("location");
                        if (locStr != null) {
                            try {
                                org.json.JSONObject json = new org.json.JSONObject(locStr);
                                edtEditAddress.setText(json.getString("address"));
                            } catch (Exception e) {
                                edtEditAddress.setText(locStr);
                            }
                        }
                    }
                });
    }

    private void saveCampaignData() {
        String newTitle = edtEditTitle.getText().toString().trim();
        String targetStr = edtEditTargetQuantity.getText().toString().trim();
        String newAddress = edtEditAddress.getText().toString().trim();
        String newDetail = edtEditDetail.getText().toString().trim();
        String priceStr = edtEditPrice.getText().toString().trim();

        if (newTitle.isEmpty() || targetStr.isEmpty() || newAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập các thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveEdit.setEnabled(false);
        btnSaveEdit.setText("Đang lưu...");

        // Đóng gói lại địa chỉ thành JSON để đồng bộ với cấu trúc cũ
        org.json.JSONObject newLocationJson = new org.json.JSONObject();
        try {
            newLocationJson.put("address", newAddress);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        db.collection("Campaigns").document(campaignId)
                .update(
                        "title", newTitle,
                        "targetQuantity", Double.parseDouble(targetStr),
                        "detail", newDetail,
                        "price", priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr),
                        "location", newLocationJson.toString() // Lưu chuỗi JSON
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveEdit.setEnabled(true);
                    btnSaveEdit.setText("Lưu thay đổi");
                });
    }
}