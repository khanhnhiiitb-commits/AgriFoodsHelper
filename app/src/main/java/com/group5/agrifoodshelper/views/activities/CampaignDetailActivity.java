package com.group5.agrifoodshelper.views.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Commitment;

import java.text.DecimalFormat;
import java.util.UUID;

public class CampaignDetailActivity extends AppCompatActivity {

    private TextView tvCampaignTitle, tvProductPrice, tvProductOrigin;
    private TextView tvProgressText, tvFarmerName, tvFarmerPhone, tvFarmerAddress, tvCampaignStory;
    private ProgressBar progressBarCampaign;
    private Button btnOpenCommitDialog;

    // Các thông số mặc định để test giao diện
    private double currentUnitPrice = 15000.0;
    private String currentCampaignId = "CAMP_01";
    private String currentUserId = "USER_SUPPORT_01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gắn giao diện XML chi tiết chiến dịch
        setContentView(R.layout.fragment_campaign_detail);

        // 1. Ánh xạ View từ layout fragment_campaign_detail.xml
        initViews();

        // 2. Bắt sự kiện bấm nút mở Popup góp đơn
        btnOpenCommitDialog.setOnClickListener(v -> showCommitmentDialog());
    }

    private void initViews() {
        tvCampaignTitle = findViewById(R.id.tvCampaignTitle);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductOrigin = findViewById(R.id.tvProductOrigin);
        tvProgressText = findViewById(R.id.tvProgressText);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvFarmerPhone = findViewById(R.id.tvFarmerPhone);
        tvFarmerAddress = findViewById(R.id.tvFarmerAddress);
        tvCampaignStory = findViewById(R.id.tvCampaignStory);
        progressBarCampaign = findViewById(R.id.progressBarCampaign);
        btnOpenCommitDialog = findViewById(R.id.btnOpenCommitDialog);
    }

    /**
     * Hiển thị Popup Dialog để người dùng nhập số kg và xác nhận góp đơn
     */
    private void showCommitmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_commit_order, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText edtCommitQuantity = dialogView.findViewById(R.id.edtCommitQuantity);
        TextView tvCommitEstimatedPrice = dialogView.findViewById(R.id.tvCommitEstimatedPrice);
        Button btnCancelCommit = dialogView.findViewById(R.id.btnCancelCommit);
        Button btnConfirmCommit = dialogView.findViewById(R.id.btnConfirmCommit);

        DecimalFormat formatter = new DecimalFormat("#,###");

        // Lắng nghe người dùng gõ số kg để tự động tính tiền tạm tính
        edtCommitQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (!input.isEmpty()) {
                    try {
                        double qty = Double.parseDouble(input);
                        double total = qty * currentUnitPrice;
                        tvCommitEstimatedPrice.setText("Tổng tiền dự tính: " + formatter.format(total) + " đ");
                    } catch (NumberFormatException e) {
                        tvCommitEstimatedPrice.setText("Tổng tiền dự tính: 0 đ");
                    }
                } else {
                    tvCommitEstimatedPrice.setText("Tổng tiền dự tính: 0 đ");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bấm nút HỦY BỎ
        btnCancelCommit.setOnClickListener(v -> dialog.dismiss());

        // Bấm nút XÁC NHẬN GÓP ĐƠN
        btnConfirmCommit.setOnClickListener(v -> {
            String qtyStr = edtCommitQuantity.getText().toString().trim();
            if (qtyStr.isEmpty()) {
                edtCommitQuantity.setError("Vui lòng nhập số kg muốn mua");
                return;
            }

            double quantity = Double.parseDouble(qtyStr);
            if (quantity <= 0) {
                edtCommitQuantity.setError("Số lượng phải lớn hơn 0");
                return;
            }

            // Đóng gói đối tượng Commitment theo đúng Model của nhóm
            String commitmentId = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();

            Commitment newCommitment = new Commitment(
                    commitmentId,
                    currentCampaignId,
                    currentUserId,
                    quantity,
                    timestamp
            );

            Toast.makeText(CampaignDetailActivity.this, "Đã góp " + quantity + " kg thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}