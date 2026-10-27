package com.group5.agrifoodshelper.repositories;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class CommitmentRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnCommitmentResult {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public void addCommitment(String campaignId, double kgToCommit, OnCommitmentResult callback) {
        DocumentReference campaignRef = db.collection("Campaigns").document(campaignId);

        campaignRef.update("currentQuantity", FieldValue.increment(kgToCommit))
                .addOnSuccessListener(unused -> {
                    checkAndCompleteCampaign(campaignRef, campaignId, callback);
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật số lượng: " + e.getMessage()));
    }

    private void checkAndCompleteCampaign(DocumentReference campaignRef, String campaignId, OnCommitmentResult callback) {
        campaignRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                Double current = document.getDouble("currentQuantity");
                Double target = document.getDouble("targetQuantity");

                if (current != null && target != null && current >= target) {
                    WriteBatch batch = db.batch();

                    batch.update(campaignRef, "status", "COMPLETED");
                    batch.update(campaignRef, "isActive", false);

                    DocumentReference mobilizationRef = db.collection("mobilizations").document();
                    Map<String, Object> mobilizationData = new HashMap<>();
                    mobilizationData.put("campaignId", campaignId);
                    mobilizationData.put("status", "PENDING");
                    mobilizationData.put("createdAt", FieldValue.serverTimestamp());

                    batch.set(mobilizationRef, mobilizationData);

                    batch.commit()
                            .addOnSuccessListener(v -> callback.onSuccess("Đạt 100%! Đã hoàn thành chiến dịch và gọi xe tải."))
                            .addOnFailureListener(e -> callback.onFailure("Lỗi hoàn thành chiến dịch: " + e.getMessage()));
                } else {
                    callback.onSuccess("Góp đơn thành công! Đang chờ đạt mục tiêu.");
                }
            }
        }).addOnFailureListener(e -> callback.onFailure("Lỗi kiểm tra tiến độ: " + e.getMessage()));
    }
}