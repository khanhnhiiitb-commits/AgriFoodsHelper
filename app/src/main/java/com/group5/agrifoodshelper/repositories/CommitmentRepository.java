package com.group5.agrifoodshelper.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.models.Commitment;
import com.group5.agrifoodshelper.models.Mobilization;

import java.util.UUID;

public class CommitmentRepository {

    private static final String TAG = "CommitmentRepository";
    private final FirebaseFirestore db;

    public interface OnCommitmentCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    public CommitmentRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Cộng dồn an toàn sản lượng bằng FieldValue.increment()
     * Đảm bảo tính toán chính xác tuyệt đối trên Server khi nhiều người thao tác cùng lúc
     */
    public void incrementCampaignQuantity(String campaignId, double quantity, OnCommitmentCallback callback) {
        DocumentReference campaignRef = db.collection("campaigns").document(campaignId);

        Log.d(TAG, "Bắt đầu gọi FieldValue.increment(" + quantity + ") cho chiến dịch: " + campaignId);


        campaignRef.update("currentQuantity", FieldValue.increment(quantity))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Đã cộng dồn thành công " + quantity + " kg vào currentQuantity.");


                    checkAndCompleteCampaign(campaignRef, campaignId, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi gọi FieldValue.increment: " + e.getMessage(), e);
                    callback.onFailure("Không thể cộng dồn số lượng: " + e.getMessage());
                });
    }


    public void submitCommitmentWithIncrement(Commitment commitment, OnCommitmentCallback callback) {
        DocumentReference commitmentRef = db.collection("commitments").document(commitment.getCommitmentId());

        // Bước 1: Lưu bản ghi Commitment của người dùng
        commitmentRef.set(commitment)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Đã lưu bản ghi Commitment: " + commitment.getCommitmentId());

                    // Bước 2: Gọi hàm increment để tăng currentQuantity mà không bị sai số
                    incrementCampaignQuantity(
                            commitment.getCampaignId(),
                            commitment.getCommittedQuantity(),
                            callback
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lưu Commitment thất bại: " + e.getMessage(), e);
                    callback.onFailure("Lưu đơn ủng hộ thất bại: " + e.getMessage());
                });
    }


    private void checkAndCompleteCampaign(DocumentReference campaignRef, String campaignId, OnCommitmentCallback callback) {
        campaignRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                callback.onSuccess("Góp đơn thành công!");
                return;
            }

            Double currentVal = snapshot.getDouble("currentQuantity");
            Double targetVal = snapshot.getDouble("targetQuantity");
            String status = snapshot.getString("status");

            // Khai báo final để dùng an toàn trong biểu thức lambda bên dưới
            final double currentQty = (currentVal != null) ? currentVal : 0.0;
            final double finalTargetQty = (targetVal != null) ? targetVal : 0.0;

            Log.d(TAG, "Kiểm tra tiến độ: " + currentQty + " / " + finalTargetQty + " kg (Trạng thái: " + status + ")");

            // Nếu gom đủ hoặc vượt chỉ tiêu (>= 100%) và chiến dịch chưa hoàn thành
            if (currentQty >= finalTargetQty && finalTargetQty > 0 && !"COMPLETED".equals(status)) {

                // Đổi trạng thái sang COMPLETED
                campaignRef.update("status", "COMPLETED")
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "Đã tự động cập nhật status chiến dịch thành COMPLETED");

                            // Tự động sinh bản ghi mới trong collection mobilizations (Lệnh gọi xe tải)
                            createMobilizationOrder(campaignId, finalTargetQty, callback);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Lỗi khi cập nhật trạng thái COMPLETED: " + e.getMessage(), e);
                            callback.onSuccess("Góp đơn thành công, nhưng gặp lỗi khi chuyển trạng thái chiến dịch.");
                        });
            } else {
                callback.onSuccess("Góp đơn giải cứu thành công!");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi đọc dữ liệu kiểm tra tiến độ: " + e.getMessage(), e);
            callback.onSuccess("Góp đơn thành công!");
        });
    }


    private void createMobilizationOrder(String campaignId, double targetWeight, OnCommitmentCallback callback) {
        String mobilizationId = UUID.randomUUID().toString();
        DocumentReference mobilizationRef = db.collection("mobilizations").document(mobilizationId);

        Mobilization truckJob = new Mobilization(
                mobilizationId,
                campaignId,
                "TRUCK",
                (int) Math.ceil(targetWeight),
                "",
                "WAITING"
        );

        mobilizationRef.set(truckJob)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Đã tự động tạo lệnh gọi xe tải (Mobilization): " + mobilizationId);
                    callback.onSuccess("Góp đơn thành công! Đã gom đủ 100% sản lượng và tự động tạo chuyến điều phối xe tải.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tạo lệnh xe tải: " + e.getMessage(), e);
                    callback.onSuccess("Góp đơn thành công! Chiến dịch hoàn thành nhưng lỗi tạo lệnh xe.");
                });
    }
}
