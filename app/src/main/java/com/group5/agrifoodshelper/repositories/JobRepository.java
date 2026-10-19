package com.group5.agrifoodshelper.repositories;

import androidx.lifecycle.MutableLiveData;
import com.group5.agrifoodshelper.models.Mobilization;
import com.group5.agrifoodshelper.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class JobRepository {
    private FirebaseFirestore firestore;

    public JobRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<List<Mobilization>> getWaitingJobs() {
        MutableLiveData<List<Mobilization>> liveData = new MutableLiveData<>();

        firestore.collection(Constants.COLL_MOBILIZATIONS)
                .whereEqualTo("status", "WAITING")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<Mobilization> jobs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        Mobilization job = doc.toObject(Mobilization.class);
                        job.setMobilizationId(doc.getId());
                        jobs.add(job);
                    }
                    liveData.postValue(jobs);
                });

        return liveData;
    }
}