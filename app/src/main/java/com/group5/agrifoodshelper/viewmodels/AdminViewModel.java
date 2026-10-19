package com.group5.agrifoodshelper.viewmodels;

import androidx.lifecycle.ViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.utils.Constants;

public class AdminViewModel extends ViewModel {
    private FirebaseFirestore firestore;

    public AdminViewModel() {
        firestore = FirebaseFirestore.getInstance();
    }

    // Hàm Admin bấm "Duyệt" -> Chuyển trạng thái từ PENDING sang ACTIVE
    public Task<Void> approveCampaign(String campaignId) {
        return firestore.collection(Constants.COLL_CAMPAIGNS)
                .document(campaignId)
                .update("status", Constants.STATUS_ACTIVE);
    }

    // Hàm Admin bấm "Từ chối / Xóa"
    public Task<Void> rejectCampaign(String campaignId) {
        // Tùy theo logic nhóm, có thể xóa thẳng document hoặc update trạng thái thành REJECTED
        return firestore.collection(Constants.COLL_CAMPAIGNS)
                .document(campaignId)
                .delete();
    }
}