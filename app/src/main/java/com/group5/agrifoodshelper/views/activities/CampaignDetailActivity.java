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
import androidx.lifecycle.ViewModelProvider;

import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.viewmodels.CommitmentViewModel;

import java.text.DecimalFormat;

public class CampaignDetailActivity extends AppCompatActivity {

    private TextView tvCampaignTitle, tvProductPrice, tvProductOrigin;
    private TextView tvProgressText, tvFarmerName, tvFarmerPhone, tvFarmerAddress, tvCampaignStory;
    private ProgressBar progressBarCampaign;
    private Button btnOpenCommitDialog;

    // 1. Khai báo ViewModel
    private CommitmentViewModel commitmentViewModel;

    // Các thông số mặc định để thử nghiệm
    private double currentUnitPrice = 15000.0;
    private String currentCampaignId = "CAMP_01";
    private String currentUserId = "USER_SUPPORT_01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_campaign_detail);

        // Ánh xạ View từ layout fragment_campaign_detail.xml
        initViews();

        // 2. Khởi tạo ViewModel
        commitmentViewModel = new ViewModelProvider(this).get(CommitmentViewModel.class);

        // 3. Đăng ký lắng nghe LiveData phản hồi từ Firebase
        observeViewModel();

        // Bắt sự kiện bấm nút mở Popup góp đơn
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

    private void observeViewModel() {
        commitmentViewModel.getSuccessMessage().observe(this, message -> {
            Toast.makeText(CampaignDetailActivity.this, message, Toast.LENGTH_LONG).show();
        });

        commitmentViewModel.getErrorMessage().observe(this, error -> {
            Toast.makeText(CampaignDetailActivity.this, error, Toast.LENGTH_SHORT).show();
        });
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

        // Sử dụng hàm tính toán trong ViewModel để tính tổng tiền tạm tính
        edtCommitQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (!input.isEmpty()) {
                    try {
                        double qty = Double.parseDouble(input);
                        double total = commitmentViewModel.calculateEstimatedPrice(qty, currentUnitPrice);
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

            // Gọi ViewModel xử lý logic kiểm tra và lưu Firestore
            commitmentViewModel.submitCommitment(currentCampaignId, currentUserId, qtyStr);

            dialog.dismiss();
        });

        dialog.show();
    }
}