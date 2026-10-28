package com.group5.agrifoodshelper.views.activities;

import android.content.Intent;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import com.group5.agrifoodshelper.R;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CampaignDetailActivity extends AppCompatActivity {

    private String campaignId = "";
    private String currentUserId = "";
    private String campaignOwnerId = "";
    private String campaignStatus = "";

    private double currentPricePerKg = 15000.0;
    private boolean isAdmin = false;
    private boolean campaignLoaded = false;

    private FirebaseFirestore db;

    private ImageView imgDetailPhoto;
    private TextView tvDetailTitle;
    private TextView tvDetailPrice;
    private TextView tvDetailAddress;
    private TextView tvDetailProgressText;
    private TextView tvFarmerInfo;
    private ProgressBar progressBarDetail;

    private MaterialButton btnOpenContribute;
    private MaterialButton btnEmergencyClose;
    private MaterialButton btnMarkSuccess;
    private MaterialButton btnManageApplicants;
    private boolean isCampaignPinned = false;
    private MaterialButton btnPinCampaign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_campaign_detail);

        db = FirebaseFirestore.getInstance();

        if (getIntent() != null && getIntent().hasExtra("CAMPAIGN_ID")) {
            campaignId = getIntent().getStringExtra("CAMPAIGN_ID");
        }

        if (campaignId == null || campaignId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy dữ liệu chiến dịch.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imgDetailPhoto = findViewById(R.id.imgDetailPhoto);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailAddress = findViewById(R.id.tvDetailAddress);
        tvDetailProgressText = findViewById(R.id.tvDetailProgressText);
        progressBarDetail = findViewById(R.id.progressBarDetail);
        tvFarmerInfo = findViewById(R.id.tvFarmerInfo);

        btnOpenContribute = findViewById(R.id.btnOpenContributeDialog);
        btnEmergencyClose = findViewById(R.id.btnEmergencyClose);
        btnMarkSuccess = findViewById(R.id.btnMarkSuccess);
        btnManageApplicants = findViewById(R.id.btnManageApplicants);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (btnEmergencyClose != null) {
            btnEmergencyClose.setVisibility(View.GONE);
            btnEmergencyClose.setOnClickListener(v -> updateCampaignStatus("CLOSED"));
        }

        if (btnMarkSuccess != null) {
            btnMarkSuccess.setVisibility(View.GONE);
            btnMarkSuccess.setOnClickListener(v -> updateCampaignStatus("SUCCESS"));
        }

        if (btnOpenContribute != null) {
            btnOpenContribute.setOnClickListener(v -> showContributeBottomSheet());
        }
        btnPinCampaign = findViewById(R.id.btnPinCampaign);
        if (btnPinCampaign != null) {
            btnPinCampaign.setOnClickListener(v -> togglePinStatus());
        }
        if (btnManageApplicants != null) {
            btnManageApplicants.setVisibility(View.GONE);
            btnManageApplicants.setOnClickListener(v -> {
                Intent intent = new Intent(CampaignDetailActivity.this, ManageApplicantsActivity.class);
                intent.putExtra("JOB_ID", campaignId);
                startActivity(intent);
            });
        }

        updateActionButtons();
        loadCampaignDetails();
        checkAdminRole();
        listenToCampaignUpdates();
    }

    private void checkAdminRole() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            isAdmin = false;
            updateActionButtons();
            return;
        }

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && hasAdminRole(document)) {
                        isAdmin = true;
                        updateActionButtons();
                    } else {
                        checkAdminByUserId();
                    }
                })
                .addOnFailureListener(e -> checkAdminByUserId());
    }

    private boolean hasAdminRole(DocumentSnapshot document) {
        String[] roleFields = {"role", "userRole", "accountType", "userType"};
        for (String field : roleFields) {
            String role = document.getString(field);
            if (role != null) {
                role = role.trim();
                if ("ADMIN".equalsIgnoreCase(role) || "ADMINISTRATOR".equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }
        Boolean isAdminValue = document.getBoolean("isAdmin");
        if (Boolean.TRUE.equals(isAdminValue)) return true;

        Boolean isAdministrator = document.getBoolean("isAdministrator");
        return Boolean.TRUE.equals(isAdministrator);
    }

    private void checkAdminByUserId() {
        db.collection("users").whereEqualTo("userId", currentUserId).limit(1).get()
                .addOnSuccessListener(result -> {
                    isAdmin = false;
                    if (!result.isEmpty()) {
                        DocumentSnapshot document = result.getDocuments().get(0);
                        isAdmin = hasAdminRole(document);
                    }
                    updateActionButtons();
                })
                .addOnFailureListener(e -> {
                    isAdmin = false;
                    updateActionButtons();
                });
    }
    private void togglePinStatus() {
        if (!isAdmin) {
            Toast.makeText(this, "Chỉ Admin mới có quyền ghim bài.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean newPinStatus = !isCampaignPinned;
        btnPinCampaign.setEnabled(false); // Khóa tạm nút trong lúc chờ mạng

        db.collection("Campaigns").document(campaignId)
                .update("isPinned", newPinStatus)
                .addOnSuccessListener(unused -> {
                    btnPinCampaign.setEnabled(true);
                    Toast.makeText(this, newPinStatus ? "Đã ghim lên đầu trang" : "Đã bỏ ghim", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnPinCampaign.setEnabled(true);
                    Toast.makeText(this, "Lỗi khi ghim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void loadCampaignDetails() {
        db.collection("Campaigns").document(campaignId).get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        Toast.makeText(CampaignDetailActivity.this, "Chiến dịch không tồn tại hoặc đã bị xóa.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String title = document.getString("title");
                    if (title != null) tvDetailTitle.setText(title);

                    Double priceValue = document.getDouble("price");
                    if (priceValue != null && priceValue > 0) {
                        currentPricePerKg = priceValue;
                    }

                    if (tvDetailPrice != null) {
                        tvDetailPrice.setText(String.format(Locale.getDefault(), "%,.0f đ/kg", currentPricePerKg));
                    }

                    tvDetailAddress.setText(getAddressText(document));
                    campaignOwnerId = getOwnerId(document);
                    campaignStatus = getStatus(document);
                    campaignLoaded = true;

                    String detail = document.getString("detail");
                    String authorName = document.getString("authorName");
                    String createdBy = document.getString("createdBy");
                    String displayName;

                    if (authorName != null && !authorName.trim().isEmpty()) {
                        displayName = authorName;
                    } else if ("ADMIN".equalsIgnoreCase(createdBy)) {
                        displayName = "Ban Quản Trị";
                    } else {
                        displayName = "Người đăng chiến dịch";
                    }

                    if (detail == null || detail.trim().isEmpty()) {
                        detail = "Chưa có mô tả chi tiết.";
                    }

                    tvFarmerInfo.setText("Người đăng: " + displayName + "\nMô tả: " + detail);

                    String imageUrl = document.getString("imageUrl");
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        Glide.with(CampaignDetailActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(imgDetailPhoto);
                    }

                    updateProgress(document);
                    updateActionButtons();
                })
                .addOnFailureListener(e -> Toast.makeText(CampaignDetailActivity.this, "Lỗi tải chiến dịch: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String getAddressText(DocumentSnapshot document) {
        Object addressObject = document.get("address");
        if (addressObject instanceof String) {
            String address = (String) addressObject;
            if (!address.trim().isEmpty()) return address;
        }
        if (addressObject instanceof Map) {
            Map<?, ?> addressMap = (Map<?, ?>) addressObject;
            Object nestedAddress = addressMap.get("address");
            if (nestedAddress != null) return nestedAddress.toString();
        }
        String location = document.getString("location");
        if (location != null && !location.trim().isEmpty()) return location;
        return "Chưa có địa chỉ";
    }

    private String getOwnerId(DocumentSnapshot document) {
        String[] ownerFields = {"ownerId", "userId", "authorId", "createdByUid"};
        for (String field : ownerFields) {
            String value = document.getString(field);
            if (value != null && !value.trim().isEmpty()) return value;
        }
        return "";
    }

    private String getStatus(DocumentSnapshot document) {
        String status = document.getString("status");
        if (status != null && !status.trim().isEmpty()) return status.trim();
        Boolean active = document.getBoolean("isActive");
        if (Boolean.TRUE.equals(active)) return "ACTIVE";
        return "";
    }

    private void listenToCampaignUpdates() {
        db.collection("Campaigns").document(campaignId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;
                    if (snapshot == null || !snapshot.exists()) {
                        campaignLoaded = false;
                        campaignStatus = "";
                        campaignOwnerId = "";
                        updateActionButtons();
                        return;
                    }

                    Double realTimePrice = snapshot.getDouble("price");
                    if (realTimePrice != null && realTimePrice > 0) {
                        currentPricePerKg = realTimePrice;
                        if (tvDetailPrice != null) {
                            tvDetailPrice.setText(String.format(Locale.getDefault(), "%,.0f đ/kg", currentPricePerKg));
                        }
                    }
                    Boolean pinnedValue = snapshot.getBoolean("isPinned");
                    isCampaignPinned = (pinnedValue != null && pinnedValue);
                    updateProgress(snapshot);
                    campaignOwnerId = getOwnerId(snapshot);
                    campaignStatus = getStatus(snapshot);
                    campaignLoaded = true;
                    updateActionButtons();
                });
    }

    private void updateProgress(DocumentSnapshot document) {
        Double currentQty = document.getDouble("currentQuantity");
        Double targetQty = document.getDouble("targetQuantity");

        if (currentQty == null) currentQty = 0.0;

        if (targetQty == null || targetQty <= 0) {
            progressBarDetail.setProgress(0);
            tvDetailProgressText.setText("Chưa có thông tin tiến độ");
            return;
        }

        double percentage = (currentQty / targetQty) * 100.0;
        progressBarDetail.setProgress((int) Math.min(100.0, percentage));
        tvDetailProgressText.setText(String.format(Locale.getDefault(), "Đã gom: %,.0f kg / %,.0f kg (%.1f%%)", currentQty, targetQty, percentage));
    }

    private boolean isCampaignOpen() {
        return "OPEN".equalsIgnoreCase(campaignStatus) || "ACTIVE".equalsIgnoreCase(campaignStatus);
    }

    private boolean isCampaignOwner() {
        return currentUserId != null && !currentUserId.isEmpty()
                && campaignOwnerId != null && !campaignOwnerId.isEmpty()
                && currentUserId.equals(campaignOwnerId);
    }

    private void updateActionButtons() {
        if (btnEmergencyClose == null || btnMarkSuccess == null || btnOpenContribute == null) return;

        boolean isOpen = isCampaignOpen();
        boolean canManage = isAdmin || isCampaignOwner();

        boolean showManageButtons = campaignLoaded && isOpen && canManage;

        btnEmergencyClose.setVisibility(showManageButtons ? View.VISIBLE : View.GONE);
        btnMarkSuccess.setVisibility(showManageButtons ? View.VISIBLE : View.GONE);
        btnOpenContribute.setVisibility(campaignLoaded && isOpen ? View.VISIBLE : View.GONE);
        if (btnPinCampaign != null) {
            btnPinCampaign.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            btnPinCampaign.setText(isCampaignPinned ? "Bỏ ghim bài viết" : "Ghim lên đầu trang");
        }
        if (btnManageApplicants != null) {
            btnManageApplicants.setVisibility(canManage ? View.VISIBLE : View.GONE);
        }
    }

    private void updateCampaignStatus(final String newStatus) {
        if (!"CLOSED".equals(newStatus) && !"SUCCESS".equals(newStatus)) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isAdmin && !isCampaignOwner()) {
            Toast.makeText(this, "Bạn không có quyền quản lý chiến dịch này.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isCampaignOpen()) {
            Toast.makeText(this, "Chiến dịch đã kết thúc hoặc chưa được mở.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("isActive", false);

        db.collection("Campaigns").document(campaignId).update(updates)
                .addOnSuccessListener(unused -> {
                    campaignStatus = newStatus;
                    updateActionButtons();
                    String message = "CLOSED".equals(newStatus) ? "Đã kết thúc khẩn cấp chiến dịch." : "Đã xác nhận giải cứu thành công.";
                    Toast.makeText(CampaignDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(CampaignDetailActivity.this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showContributeBottomSheet() {
        if (!isCampaignOpen()) {
            Toast.makeText(this, "Chiến dịch đã kết thúc, không thể góp đơn.", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.dialog_commit_order, null);
        dialog.setContentView(sheetView);

        TextInputEditText edtContributionKg = sheetView.findViewById(R.id.edtContributionKg);
        TextView tvTotalPrice = sheetView.findViewById(R.id.tvTotalPrice);
        MaterialButton btnCancel = sheetView.findViewById(R.id.btnCancelContribute);
        final MaterialButton btnConfirm = sheetView.findViewById(R.id.btnConfirmContribute);
        final DecimalFormat formatter = new DecimalFormat("#,###");

        edtContributionKg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String kgString = edtContributionKg.getText().toString().trim();
            if (kgString.isEmpty()) {
                edtContributionKg.setError("Vui lòng nhập số kg");
                return;
            }

            double kg;
            try {
                kg = Double.parseDouble(kgString);
            } catch (NumberFormatException e) {
                edtContributionKg.setError("Số kg không hợp lệ");
                return;
            }

            if (kg <= 0) {
                edtContributionKg.setError("Số kg phải lớn hơn 0");
                return;
            }

            processContribution(kg, dialog, btnConfirm);
        });

        dialog.show();
    }

    private void processContribution(final double kg, final BottomSheetDialog dialog, final MaterialButton btnConfirm) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để góp đơn.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("ĐANG TẠO THANH TOÁN VNPAY...");

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DocumentReference campaignRef = db.collection("Campaigns").document(campaignId);

        campaignRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                showContributionError(btnConfirm, "Chiến dịch không còn tồn tại.");
                return;
            }

            String latestStatus = getStatus(snapshot);
            if (!"OPEN".equalsIgnoreCase(latestStatus) && !"ACTIVE".equalsIgnoreCase(latestStatus)) {
                campaignStatus = latestStatus;
                updateActionButtons();
                Toast.makeText(CampaignDetailActivity.this, "Chiến dịch đã kết thúc, không thể góp đơn.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            long tongTien = (long) (kg * currentPricePerKg);
            String orderInfo = "Giai cuu " + kg + "kg nong san";

            String payUrl = com.group5.agrifoodshelper.utils.VNPayPaymentHelper.createPaymentUrl(tongTien, orderInfo);

            saveContributionToFirestore(kg, tongTien, userId, campaignRef, dialog, btnConfirm);

            android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(payUrl));
            startActivity(browserIntent);

        }).addOnFailureListener(e -> showContributionError(btnConfirm, "Không kiểm tra được trạng thái: " + e.getMessage()));
    }

    private void saveContributionToFirestore(double kg, long tongTien, String userId, DocumentReference campaignRef, BottomSheetDialog dialog, MaterialButton btnConfirm) {
        Map<String, Object> orderRecord = new HashMap<>();
        orderRecord.put("campaignId", campaignId);
        orderRecord.put("userId", userId);
        orderRecord.put("quantityKg", kg);
        orderRecord.put("totalPrice", (double) tongTien);
        orderRecord.put("timestamp", System.currentTimeMillis());

        campaignRef.collection("Contributions").add(orderRecord)
                .addOnSuccessListener(reference -> {
                    campaignRef.update("currentQuantity", FieldValue.increment(kg))
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(CampaignDetailActivity.this, "Đang chuyển hướng thanh toán...", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> showContributionError(btnConfirm, "Đã lưu đơn nhưng không cập nhật được tiến độ: " + e.getMessage()));
                })
                .addOnFailureListener(e -> showContributionError(btnConfirm, "Lỗi lưu đóng góp: " + e.getMessage()));
    }

    private void showContributionError(MaterialButton btnConfirm, String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        btnConfirm.setEnabled(true);
        btnConfirm.setText("XÁC NHẬN GÓP ĐƠN");
    }
}