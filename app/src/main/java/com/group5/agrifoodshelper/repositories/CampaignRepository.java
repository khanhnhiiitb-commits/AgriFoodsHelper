package com.group5.agrifoodshelper.repositories;

import androidx.lifecycle.MutableLiveData;
import com.group5.agrifoodshelper.models.Campaign;
import com.group5.agrifoodshelper.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CampaignRepository {
    private FirebaseFirestore firestore;

    public CampaignRepository() {
        firestore = FirebaseFirestore.getInstance();
    }
/*
    public MutableLiveData<List<Campaign>> getActiveCampaigns() {
        MutableLiveData<List<Campaign>> liveData = new MutableLiveData<>();

        // Lấy danh sách các chiến dịch ĐÃ DUYỆT (ACTIVE)
        firestore.collection(Constants.COLL_CAMPAIGNS)
                .whereEqualTo("status", Constants.STATUS_ACTIVE)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }

                    List<Campaign> campaigns = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        Campaign c = doc.toObject(Campaign.class);
                        c.campaignId = doc.getId();
                        campaigns.add(c);
                    }
                    liveData.postValue(campaigns);
                });

        return liveData;
    }
    */

}