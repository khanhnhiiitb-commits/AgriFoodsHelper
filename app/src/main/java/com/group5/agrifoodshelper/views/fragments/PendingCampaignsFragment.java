package com.group5.agrifoodshelper.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.FarmerReport; // Đổi import sang FarmerReport
import com.group5.agrifoodshelper.views.adapters.PendingCampaignAdapter;
import java.util.ArrayList;
import java.util.List;

public class PendingCampaignsFragment extends Fragment {

    private RecyclerView recyclerPending;
    private PendingCampaignAdapter adapter;
    private List<FarmerReport> pendingList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pending_campaigns, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nút quay lại Bảng điều khiển Admin
        ImageView btnBack = view.findViewById(R.id.btnBackFromPending);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
            });
        }

        // Cài đặt RecyclerView
        recyclerPending = view.findViewById(R.id.recyclerPendingCampaigns);
        recyclerPending.setLayoutManager(new LinearLayoutManager(getContext()));

        pendingList = new ArrayList<>();
        adapter = new PendingCampaignAdapter();
        recyclerPending.setAdapter(adapter);

        // Tải dữ liệu từ Firebase
        loadPendingCampaigns();
    }

    private void loadPendingCampaigns() {
        db = FirebaseFirestore.getInstance();

        // Chỉ lấy những yêu cầu đang có trạng thái PENDING từ collection farmer_reports
        db.collection("farmer_reports")
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pendingList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Ép kiểu JSON từ Firebase về đúng model FarmerReport
                        FarmerReport report = doc.toObject(FarmerReport.class);
                        if (report != null) {
                            report.setReportId(doc.getId());
                            pendingList.add(report);
                        }
                    }
                    adapter.setPendingList(pendingList);

                    if (pendingList.isEmpty() && getContext() != null) {
                        Toast.makeText(getContext(), "Hiện không có bài đăng nào cần duyệt", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}