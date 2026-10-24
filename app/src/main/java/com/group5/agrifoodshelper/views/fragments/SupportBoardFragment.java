package com.group5.agrifoodshelper.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.SupportJob;
import com.group5.agrifoodshelper.views.adapters.SupportJobAdapter;
import java.util.ArrayList;
import java.util.List;

public class SupportBoardFragment extends Fragment {

    private RecyclerView rvSupportJobs;
    private SupportJobAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_support_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSupportJobs = view.findViewById(R.id.rvSupportJobs);
        rvSupportJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SupportJobAdapter();
        rvSupportJobs.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadSupportJobs();
    }

    private void loadSupportJobs() {
        db.collection("support_jobs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SupportJob> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        SupportJob job = doc.toObject(SupportJob.class);
                        list.add(job);
                    }
                    adapter.setJobList(list);
                });
    }
}