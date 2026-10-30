package com.group5.agrifoodshelper.repositories;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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
                    callback.onSuccess("Góp đơn thành công!");
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật số lượng: " + e.getMessage()));
    }
}