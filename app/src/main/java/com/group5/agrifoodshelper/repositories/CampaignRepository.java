package com.group5.agrifoodshelper.repositories;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.group5.agrifoodshelper.models.Campaign;
import java.util.ArrayList;
import java.util.List;

public class CampaignRepository {

    private FirebaseFirestore db;

    public CampaignRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Campaign>> getActiveCampaigns() {
        MutableLiveData<List<Campaign>> campaignData = new MutableLiveData<>();
        db.collection("Campaigns")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Campaign> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Campaign campaign = document.toObject(Campaign.class);
                            campaign.setCampaignId(document.getId());
                            list.add(campaign);
                        } catch (Exception e) {
                            Log.e("CampaignRepository", "Lỗi parse dữ liệu: " + e.getMessage());
                        }
                    }

                    Log.d("DEBUG_CAMPAIGN", "Tổng số bài lấy về từ Firebase: " + list.size());


                    java.util.Collections.sort(list, (c1, c2) -> {
                        boolean p1 = c1 != null && c1.isPinned();
                        boolean p2 = c2 != null && c2.isPinned();

                        // Nếu trạng thái ghim khác nhau, đưa bài được ghim lên trên
                        if (p1 != p2) {
                            return Boolean.compare(p2, p1);
                        }

                        // Nếu cùng trạng thái ghim, sắp xếp theo thời gian mới nhất (createdAt)
                        long t1 = c1 != null ? c1.getCreatedAt() : 0;
                        long t2 = c2 != null ? c2.getCreatedAt() : 0;
                        return Long.compare(t2, t1);
                    });

                    campaignData.postValue(list);
                })
                .addOnFailureListener(e -> {
                    Log.e("CampaignRepository", "Lỗi tải dữ liệu từ Firebase: " + e.getMessage());
                    campaignData.postValue(new ArrayList<>());
                });

        return campaignData;
    }
}