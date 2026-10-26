package com.group5.agrifoodshelper.repositories;

import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.models.JobApplication;

public class JobApplicationRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OperationCallback {
        void onSuccess();
        void onError(String error);
    }

    // Nộp đơn ứng tuyển (Status: PENDING)
    public void submitApplication(JobApplication application, OperationCallback callback) {
        db.collection("job_applications")
                .document(application.getApplicationId())
                .set(application)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Cập nhật trạng thái đơn (Duyệt/Từ chối)
    public void updateApplicationStatus(String applicationId, String newStatus, OperationCallback callback) {
        db.collection("job_applications")
                .document(applicationId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}