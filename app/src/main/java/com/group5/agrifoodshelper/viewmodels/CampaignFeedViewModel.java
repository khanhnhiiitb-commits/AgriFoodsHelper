package com.group5.agrifoodshelper.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.group5.agrifoodshelper.data.local.AppDatabase;
import com.group5.agrifoodshelper.models.Campaign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CampaignFeedViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Campaign>> campaignListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final AppDatabase roomDb;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CampaignFeedViewModel(@NonNull Application application) {
        super(application);
        roomDb = AppDatabase.getInstance(application);
    }

    public LiveData<List<Campaign>> getCampaigns() {
        return campaignListLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public void fetchCampaigns(boolean isNetworkAvailable) {
        isLoadingLiveData.setValue(true);

        if (isNetworkAvailable) {
            firestore.collection("Campaigns")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Campaign> onlineCampaigns = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                Campaign c = doc.toObject(Campaign.class);
                                c.setCampaignId(doc.getId());
                                onlineCampaigns.add(c);
                            } catch (Exception e) {
                                android.util.Log.e("CampaignFeedVM", "Lỗi parse: " + e.getMessage());
                            }
                        }

                        // Sắp xếp an toàn: Ghim lên đầu, sau đó sắp xếp theo thời gian mới nhất
                        sortCampaigns(onlineCampaigns);

                        campaignListLiveData.setValue(onlineCampaigns);
                        isLoadingLiveData.setValue(false);

                        // Lưu vào Room để dùng offline
                        executor.execute(() -> {
                            roomDb.campaignDao().clearAll();
                            roomDb.campaignDao().insertCampaigns(onlineCampaigns);
                        });
                    })
                    .addOnFailureListener(e -> {
                        isLoadingLiveData.setValue(false);
                        android.util.Log.e("CampaignFeedVM", "Lỗi tải Firebase: " + e.getMessage());
                    });
        } else {
            // Đọc offline từ Room khi mất mạng
            executor.execute(() -> {
                List<Campaign> offlineCampaigns = roomDb.campaignDao().getOfflineCampaigns();
                if (offlineCampaigns != null) {
                    sortCampaigns(offlineCampaigns);
                }
                // Đẩy về Main Thread
                campaignListLiveData.postValue(offlineCampaigns != null ? offlineCampaigns : new ArrayList<>());
                isLoadingLiveData.postValue(false);
            });
        }
    }

    private void sortCampaigns(List<Campaign> list) {
        Collections.sort(list, (c1, c2) -> {
            boolean p1 = c1 != null && c1.isPinned();
            boolean p2 = c2 != null && c2.isPinned();
            if (p1 != p2) {
                return Boolean.compare(p2, p1);
            }
            long t1 = c1 != null ? c1.getCreatedAt() : 0;
            long t2 = c2 != null ? c2.getCreatedAt() : 0;
            return Long.compare(t2, t1);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}