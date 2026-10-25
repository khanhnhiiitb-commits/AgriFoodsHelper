package com.group5.agrifoodshelper.repositories;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
        db = FirebaseFirestore.getInstance();
    }

    public void submitCommitmentWithIncrement(
            final Commitment commitment,
            final OnCommitmentCallback callback) {

        final DocumentReference campaignRef = db.collection("Campaigns")
                .document(commitment.getCampaignId());

        // Kiểm tra chiến dịch trước khi nhận đóng góp
        campaignRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onFailure("Chiến dịch không tồn tại.");
                            return;
                        }

                        String status = snapshot.getString("status");

                        if (status == null || !"OPEN".equalsIgnoreCase(status)) {
                            callback.onFailure(
                                    "Chiến dịch đã kết thúc, không thể góp đơn.");
                            return;
                        }

                        saveCommitment(commitment, callback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Lỗi kiểm tra chiến dịch: "
                                + e.getMessage(), e);

                        callback.onFailure(
                                "Không thể kiểm tra chiến dịch: "
                                        + e.getMessage());
                    }
                });
    }

    private void saveCommitment(
            final Commitment commitment,
            final OnCommitmentCallback callback) {

        DocumentReference commitmentRef = db.collection("commitments")
                .document(commitment.getCommitmentId());

        commitmentRef.set(commitment)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Đã lưu Commitment: "
                                + commitment.getCommitmentId());

                        incrementCampaignQuantity(
                                commitment.getCampaignId(),
                                commitment.getCommittedQuantity(),
                                callback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Lỗi lưu Commitment: "
                                + e.getMessage(), e);

                        callback.onFailure(
                                "Lưu đơn ủng hộ thất bại: "
                                        + e.getMessage());
                    }
                });
    }

    public void incrementCampaignQuantity(
            final String campaignId,
            final double quantity,
            final OnCommitmentCallback callback) {

        final DocumentReference campaignRef = db.collection("Campaigns")
                .document(campaignId);

        // Kiểm tra trạng thái một lần nữa trước khi cộng số lượng
        campaignRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onFailure("Chiến dịch không tồn tại.");
                            return;
                        }

                        String status = snapshot.getString("status");

                        if (status == null || !"OPEN".equalsIgnoreCase(status)) {
                            callback.onFailure(
                                    "Chiến dịch đã kết thúc, không thể góp đơn.");
                            return;
                        }

                        campaignRef.update(
                                        "currentQuantity",
                                        FieldValue.increment(quantity))
                                .addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d(TAG,
                                                        "Đã cộng dồn "
                                                                + quantity
                                                                + " kg.");

                                                checkAndCompleteCampaign(
                                                        campaignRef,
                                                        campaignId,
                                                        callback);
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception e) {
                                                Log.e(TAG,
                                                        "Lỗi cập nhật số lượng: "
                                                                + e.getMessage(),
                                                        e);

                                                callback.onFailure(
                                                        "Không thể cộng dồn số lượng: "
                                                                + e.getMessage());
                                            }
                                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(
                                "Không thể kiểm tra trạng thái chiến dịch.");
                    }
                });
    }

    private void checkAndCompleteCampaign(
            final DocumentReference campaignRef,
            final String campaignId,
            final OnCommitmentCallback callback) {

        campaignRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onSuccess("Góp đơn thành công!");
                            return;
                        }

                        Double currentVal =
                                snapshot.getDouble("currentQuantity");
                        Double targetVal =
                                snapshot.getDouble("targetQuantity");
                        String status = snapshot.getString("status");

                        double currentQty =
                                currentVal != null ? currentVal : 0.0;
                        double targetQty =
                                targetVal != null ? targetVal : 0.0;

                        // Chỉ tạo đơn vận chuyển khi vẫn OPEN và đủ số lượng.
                        // Không tự đổi status thành COMPLETED.
                        if ("OPEN".equalsIgnoreCase(status)
                                && targetQty > 0
                                && currentQty >= targetQty) {

                            createMobilizationOrder(
                                    campaignId, targetQty, callback);
                        } else {
                            callback.onSuccess(
                                    "Góp đơn giải cứu thành công!");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG,
                                "Lỗi kiểm tra tiến độ: " + e.getMessage(), e);

                        callback.onSuccess("Góp đơn thành công!");
                    }
                });
    }

    private void createMobilizationOrder(
            String campaignId,
            double targetWeight,
            final OnCommitmentCallback callback) {

        String mobilizationId = UUID.randomUUID().toString();

        DocumentReference mobilizationRef =
                db.collection("mobilizations").document(mobilizationId);

        Mobilization truckJob = new Mobilization(
                mobilizationId,
                campaignId,
                "TRUCK",
                (int) Math.ceil(targetWeight),
                "",
                "WAITING"
        );

        mobilizationRef.set(truckJob)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(
                                "Góp đơn thành công! Đã đủ số lượng và tạo chuyến xe tải.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG,
                                "Lỗi tạo đơn vận chuyển: " + e.getMessage(), e);

                        callback.onSuccess(
                                "Góp đơn thành công nhưng chưa tạo được chuyến xe.");
                    }
                });
    }
}
