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
                            // Chuyển đổi dữ liệu từ Firebase thành đối tượng Campaign của bạn
                            Campaign campaign = document.toObject(Campaign.class);
                            list.add(campaign);
                        } catch (Exception e) {
                            Log.e("CampaignRepository", "Lỗi parse dữ liệu: " + e.getMessage());
                        }
                    }
                    // 3. Đưa danh sách vừa lấy được vào LiveData để truyền lên giao diện
                    campaignData.postValue(list);
                })
                .addOnFailureListener(e -> {
                    Log.e("CampaignRepository", "Lỗi tải dữ liệu từ Firebase: " + e.getMessage());
                    // Nếu lỗi mạng, trả về mảng rỗng để app vẫn chạy tiếp mà không bị sập
                    campaignData.postValue(new ArrayList<>());
                });

        // 4. Trả về hộp chứa LiveData (lúc này hộp không còn rỗng nên sẽ không gây lỗi dòng 61 nữa)
        return campaignData;
    }
}