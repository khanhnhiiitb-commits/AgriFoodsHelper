package com.group5.agrifoodshelper.views.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Campaign;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class CampaignDetailActivity extends AppCompatActivity {

    private String campaignId;
    private double currentPricePerKg = 15000.0; // Tạm fix giá, nếu model có field giá thì update sau
    private FirebaseFirestore db;

    // Các View trên giao diện
    private ImageView imgDetailPhoto;
    private TextView tvDetailTitle, tvDetailPrice, tvDetailAddress, tvDetailProgressText, tvFarmerInfo;
    private ProgressBar progressBarDetail;
    private MaterialButton btnOpenContribute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_campaign_detail);

        db = FirebaseFirestore.getInstance();

        // 1. Nhận ID từ Intent
        if (getIntent() != null && getIntent().hasExtra("CAMPAIGN_ID")) {
            campaignId = getIntent().getStringExtra("CAMPAIGN_ID");
        } else {
            Toast.makeText(this, "Không tìm thấy dữ liệu chiến dịch", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Ánh xạ các View
        imgDetailPhoto = findViewById(R.id.imgDetailPhoto);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailAddress = findViewById(R.id.tvDetailAddress);
        tvDetailProgressText = findViewById(R.id.tvDetailProgressText);
        progressBarDetail = findViewById(R.id.progressBarDetail);
        tvFarmerInfo = findViewById(R.id.tvFarmerInfo);
        btnOpenContribute = findViewById(R.id.btnOpenContributeDialog);

        btnOpenContribute.setOnClickListener(v -> showContributeBottomSheet());

        // 3. Tải thông tin chi tiết từ Firebase
        loadCampaignDetails();

        // 4. Lắng nghe tiến độ đóng góp realtime (như cũ)
        listenToCampaignUpdates();
    }

    // Hàm MỚI: Tải dữ liệu từ Firestore và đắp lên giao diện
    private void loadCampaignDetails() {
        db.collection("Campaigns").document(campaignId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Campaign campaign = documentSnapshot.toObject(Campaign.class);

                        if (campaign != null) {
                            // Đắp Tên và Địa chỉ
                            tvDetailTitle.setText(campaign.getTitle());
                            String locationStr = documentSnapshot.getString("location"); // Tên trường trên Firebase
                            if (locationStr != null) {
                                try {
                                    org.json.JSONObject jsonObject = new org.json.JSONObject(locationStr);
                                    String realAddress = jsonObject.getString("address");
                                    tvDetailAddress.setText(realAddress);
                                } catch (org.json.JSONException e) {
                                    tvDetailAddress.setText(locationStr);
                                }
                            }
                            // ĐÃ SỬA: Đắp thông tin chủ vườn (Dùng cho Admin)
                            String farmerInfo = "Người đăng: Ban Quản Trị (Chiến dịch khẩn cấp)\n"
                                    + "Mô tả: " + (campaign.getDetail() != null ? campaign.getDetail() : "Chưa có mô tả chi tiết.");
                            tvFarmerInfo.setText(farmerInfo);

                            // Load Hình ảnh
                            String imageUrl = campaign.getImageUrl();
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_launcher_background)
                                        .into(imgDetailPhoto);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Chiến dịch không tồn tại hoặc đã bị xóa.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Hàm lắng nghe tiến độ chiến dịch theo thời gian thực
    private void listenToCampaignUpdates() {
        if (campaignId == null) return;

        db.collection("Campaigns").document(campaignId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        return; // Bỏ qua nếu có lỗi mạng
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Double currentQty = documentSnapshot.getDouble("currentQuantity");
                        Double targetQty = documentSnapshot.getDouble("targetQuantity");

                        // Xử lý null an toàn
                        if (currentQty == null) currentQty = 0.0;

                        if (targetQty != null && targetQty > 0) {
                            double percentage = (currentQty / targetQty) * 100.0;
                            progressBarDetail.setProgress((int) percentage);
                            tvDetailProgressText.setText(String.format("Đã gom: %,.0f kg / %,.0f kg (%.1f%%)",
                                    currentQty, targetQty, percentage));
                        }
                    }
                });
    }

    // Hàm hiển thị Form đóng góp (Bottom Sheet)
    private void showContributeBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.dialog_commit_order, null);
        bottomSheetDialog.setContentView(sheetView);

        TextInputEditText edtContributionKg = sheetView.findViewById(R.id.edtContributionKg);
        TextView tvTotalPrice = sheetView.findViewById(R.id.tvTotalPrice);
        MaterialButton btnCancel = sheetView.findViewById(R.id.btnCancelContribute);
        MaterialButton btnConfirm = sheetView.findViewById(R.id.btnConfirmContribute);

        DecimalFormat formatter = new DecimalFormat("#,###");

        edtContributionKg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    tvTotalPrice.setText("Tổng tiền dự tính: 0 đ");
                    return;
                }
                try {
                    double kg = Double.parseDouble(s.toString().trim());
                    double total = kg * currentPricePerKg;
                    tvTotalPrice.setText("Tổng tiền dự tính: " + formatter.format(total) + " đ");
                } catch (NumberFormatException e) {
                    tvTotalPrice.setText("Tổng tiền dự tính: 0 đ");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String kgStr = edtContributionKg.getText().toString().trim();
            if (kgStr.isEmpty()) {
                edtContributionKg.setError("Vui lòng nhập số kg");
                return;
            }

            double kgToContribute = Double.parseDouble(kgStr);
            processContribution(kgToContribute, bottomSheetDialog, btnConfirm);
        });

        bottomSheetDialog.show();
    }

    // Hàm xử lý đẩy dữ liệu đóng góp lên Firebase
    private void processContribution(double kgToContribute, BottomSheetDialog dialog, MaterialButton btnConfirm) {
        btnConfirm.setEnabled(false);
        btnConfirm.setText("ĐANG XỬ LÝ...");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "UNKNOWN";

        Map<String, Object> orderRecord = new HashMap<>();
        orderRecord.put("campaignId", campaignId);
        orderRecord.put("userId", userId);
        orderRecord.put("quantityKg", kgToContribute);
        orderRecord.put("totalPrice", kgToContribute * currentPricePerKg);
        orderRecord.put("timestamp", System.currentTimeMillis());

        db.collection("Campaigns").document(campaignId)
                .collection("Contributions")
                .add(orderRecord)
                .addOnSuccessListener(documentReference -> {
                    db.collection("Campaigns").document(campaignId)
                            .update("currentQuantity", FieldValue.increment(kgToContribute))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Cảm ơn bạn đã đóng góp thành công!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("XÁC NHẬN GÓP ĐƠN");
                });
    }
}