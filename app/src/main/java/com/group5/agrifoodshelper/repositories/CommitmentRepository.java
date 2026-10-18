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

    public void submitCommitmentWithIncrement(Commitment commitment, OnCommitmentCallback callback) {
        DocumentReference commitmentRef = db.collection("commitments").document(commitment.getCommitmentId());

        commitmentRef.set(commitment)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Đã lưu bản ghi Commitment: " + commitment.getCommitmentId());
                    incrementCampaignQuantity(commitment.getCampaignId(), commitment.getCommittedQuantity(), callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi lưu Commitment: " + e.getMessage(), e);
                    callback.onFailure("Lưu đơn ủng hộ thất bại: " + e.getMessage());
                });
    }

    public void incrementCampaignQuantity(String campaignId, double quantity, OnCommitmentCallback callback) {
        DocumentReference campaignRef = db.collection("campaigns").document(campaignId);

        campaignRef.update("currentQuantity", FieldValue.increment(quantity))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Đã cộng dồn thành công " + quantity + " kg.");
                    checkAndCompleteCampaign(campaignRef, campaignId, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi increment: " + e.getMessage(), e);
                    callback.onFailure("Không thể cộng dồn số lượng: " + e.getMessage());
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

            final double currentQty = (currentVal != null) ? currentVal : 0.0;
            final double finalTargetQty = (targetVal != null) ? targetVal : 0.0;

            if (currentQty >= finalTargetQty && finalTargetQty > 0 && !"COMPLETED".equals(status)) {
                campaignRef.update("status", "COMPLETED")
                        .addOnSuccessListener(unused -> createMobilizationOrder(campaignId, finalTargetQty, callback))
                        .addOnFailureListener(e -> callback.onSuccess("Góp đơn thành công, lỗi chuyển trạng thái."));
            } else {
                callback.onSuccess("Góp đơn giải cứu thành công!");
            }
        }).addOnFailureListener(e -> callback.onSuccess("Góp đơn thành công!"));
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
                .addOnSuccessListener(unused -> callback.onSuccess("Góp đơn thành công! Đã gom đủ 100% và tạo chuyến xe tải."))
                .addOnFailureListener(e -> callback.onSuccess("Góp đơn thành công nhưng lỗi tạo xe."));
    }
}