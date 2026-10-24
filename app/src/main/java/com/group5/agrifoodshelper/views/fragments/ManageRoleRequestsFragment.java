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
import com.group5.agrifoodshelper.models.RoleRequest;
import com.group5.agrifoodshelper.views.adapters.RoleRequestAdapter;
import java.util.ArrayList;
import java.util.List;

public class ManageRoleRequestsFragment extends Fragment {

    private RecyclerView rvRequests;
    private RoleRequestAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = new RecyclerView(requireContext());
        rv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rv.setPadding(16, 16, 16, 16);
        return rv;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRequests = (RecyclerView) view;
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RoleRequestAdapter();
        rvRequests.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        db.collection("role_requests")
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RoleRequest> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        RoleRequest request = doc.toObject(RoleRequest.class);
                        list.add(request);
                    }
                    adapter.setRequestList(list);
                });
    }
}