
package com.group5.agrifoodshelper.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.group5.agrifoodshelper.data.remote.FirebaseManager;
import com.group5.agrifoodshelper.models.Campaign;
import com.group5.agrifoodshelper.models.Mobilization;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

public class JobBoardViewModel extends ViewModel {

    private final FirebaseFirestore firestore;

    private final MutableLiveData<List<JobBoardItem>> jobList =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> message =
            new MutableLiveData<>();

    public JobBoardViewModel() {
        firestore = FirebaseManager.getInstance().getFirestore();
    }

    public LiveData<List<JobBoardItem>> getJobList() {
        return jobList;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    // Lấy các chuyến xe đang chờ tài xế nhận
    public void loadJobs() {
        loading.setValue(true);
        message.setValue(null);

        firestore.collection("Mobilizations")
                .whereEqualTo("type", "TRUCK")
                .whereEqualTo("status", "PENDING_DRIVER")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<Task<JobBoardItem>> tasks = new ArrayList<>();

                    for (QueryDocumentSnapshot document
                            : querySnapshot) {

                        Mobilization mobilization =
                                document.toObject(Mobilization.class);

                        if (mobilization.getMobilizationId() == null) {
                            mobilization.setMobilizationId(
                                    document.getId()
                            );
                        }

                        String campaignId =
                                mobilization.getCampaignId();

                        if (campaignId == null ||
                                campaignId.isEmpty()) {
                            continue;
                        }

                        Task<JobBoardItem> task =
                                firestore.collection("Campaigns")
                                        .document(campaignId)
                                        .get()
                                        .continueWith(campaignTask -> {

                                            if (!campaignTask.isSuccessful()) {
                                                throw campaignTask.getException();
                                            }

                                            DocumentSnapshot campaignDocument =
                                                    campaignTask.getResult();

                                            if (campaignDocument == null ||
                                                    !campaignDocument.exists()) {
                                                return null;
                                            }

                                            Campaign campaign =
                                                    campaignDocument.toObject(
                                                            Campaign.class
                                                    );

                                            if (campaign == null) {
                                                return null;
                                            }

                                            if (campaign.getCampaignId() == null) {
                                                campaign.setCampaignId(
                                                        campaignDocument.getId()
                                                );
                                            }

                                            return new JobBoardItem(
                                                    mobilization,
                                                    campaign
                                            );
                                        });

                        tasks.add(task);
                    }

                    Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(results -> {

                                List<JobBoardItem> items =
                                        new ArrayList<>();

                                for (Object result : results) {
                                    JobBoardItem item =
                                            (JobBoardItem) result;

                                    if (item != null) {
                                        items.add(item);
                                    }
                                }

                                jobList.setValue(items);
                                loading.setValue(false);

                                if (items.isEmpty()) {
                                    message.setValue(
                                            "Hiện chưa có chuyến xe cần điều phối."
                                    );
                                }
                            })
                            .addOnFailureListener(e -> {
                                loading.setValue(false);
                                message.setValue(
                                        "Không thể tải thông tin chiến dịch."
                                );
                            });
                })
                .addOnFailureListener(e -> {
                    loading.setValue(false);
                    message.setValue(
                            "Không thể tải danh sách chuyến xe."
                    );
                });
    }

    // Tài xế nhận chuyến
    public void acceptJob(
            String mobilizationId,
            String driverId
    ) {
        if (mobilizationId == null ||
                mobilizationId.isEmpty() ||
                driverId == null ||
                driverId.isEmpty()) {

            message.setValue("Thông tin tài xế hoặc chuyến xe không hợp lệ.");
            return;
        }

        loading.setValue(true);
        message.setValue(null);

        firestore.runTransaction((Transaction.Function<Boolean>) transaction -> {

            com.google.firebase.firestore.DocumentReference jobRef =
                    firestore.collection("Mobilizations")
                            .document(mobilizationId);

            DocumentSnapshot document =
                    transaction.get(jobRef);

            if (!document.exists()) {
                return false;
            }

            String status = document.getString("status");
            String currentDriverId = document.getString("driverId");
            String type = document.getString("type");

            if (!"TRUCK".equals(type) ||
                    !"WAITING".equals(status) ||
                    (currentDriverId != null &&
                            !currentDriverId.isEmpty())) {
                return false;
            }

            transaction.update(jobRef, "driverId", driverId);
            transaction.update(jobRef, "status", "CONFIRMED");

            return true;

        }).addOnSuccessListener(accepted -> {

            loading.setValue(false);

            if (accepted) {
                message.setValue("Nhận chuyến thành công.");
                loadJobs();
            } else {
                message.setValue(
                        "Chuyến này đã được nhận hoặc không còn khả dụng."
                );
                loadJobs();
            }

        }).addOnFailureListener(e -> {
            loading.setValue(false);
            message.setValue("Không thể nhận chuyến. Vui lòng thử lại.");
        });
    }

    // Gói thông tin chuyến xe và chiến dịch để hiển thị trên một card
    public static class JobBoardItem {

        private final Mobilization mobilization;
        private final Campaign campaign;

        public JobBoardItem(
                Mobilization mobilization,
                Campaign campaign
        ) {
            this.mobilization = mobilization;
            this.campaign = campaign;
        }

        public Mobilization getMobilization() {
            return mobilization;
        }

        public Campaign getCampaign() {
            return campaign;
        }
    }
}
