package com.group5.agrifoodshelper.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.data.local.AppDatabase;
import com.group5.agrifoodshelper.models.Campaign;
import com.group5.agrifoodshelper.viewmodels.CampaignFeedViewModel;
import com.group5.agrifoodshelper.views.activities.CampaignDetailActivity;
import com.group5.agrifoodshelper.views.adapters.CampaignAdapter;

import java.util.List;

public class HomeFeedFragment extends Fragment {

    private CampaignFeedViewModel viewModel;
    private RecyclerView recyclerView;
    private CampaignAdapter adapter;
    private ProgressBar loadingIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_feed, container, false);
    }
    private void loadCampaigns() {
        AppDatabase roomDb = AppDatabase.getInstance(requireContext());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (isNetworkAvailable(requireContext())) {
            // TRƯỜNG HỢP 1: CÓ MẠNG -> Kéo dữ liệu mới nhất từ Cloud
            firestore.collection("Campaigns")
                    .whereEqualTo("status", "ACTIVE") // Khớp với Mục 4.3.a trong báo cáo
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Campaign> onlineCampaigns = queryDocumentSnapshots.toObjects(Campaign.class);

                        // 1. Cập nhật giao diện (Đã sửa tên biến thành adapter)
                        adapter.setCampaignList(onlineCampaigns);

                        // 2. Ghi đè vào SQLite để dành đọc Offline
                        new Thread(() -> {
                            roomDb.campaignDao().clearAll();
                            roomDb.campaignDao().insertCampaigns(onlineCampaigns);
                        }).start();
                    });
        } else {
            // TRƯỜNG HỢP 2: MẤT MẠNG -> Lấy dữ liệu từ bộ nhớ đệm SQLite
            Toast.makeText(getContext(), "Đang hiển thị ở chế độ Ngoại tuyến", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                List<Campaign> offlineCampaigns = roomDb.campaignDao().getOfflineCampaigns();

                // Đẩy dữ liệu về Main Thread để vẽ lên màn hình
                requireActivity().runOnUiThread(() -> {
                    // Đã sửa tên biến thành adapter và đổi tên hàm thành setCampaignList
                    adapter.setCampaignList(offlineCampaigns);
                });
            }).start();
        }
    }

    // Hàm hỗ trợ kiểm tra kết nối mạng
    private boolean isNetworkAvailable(android.content.Context context) {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewFeed);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);

        // KIỂM TRA HƯỚNG MÀN HÌNH ĐỂ RESPONSIVE
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        adapter = new CampaignAdapter();




        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(campaign -> {
            if (getContext() != null) {
                android.content.Intent intent = new android.content.Intent(getContext(), CampaignDetailActivity.class);
                intent.putExtra("CAMPAIGN_ID", campaign.getCampaignId());
                startActivity(intent);
            }
        });

        viewModel = new ViewModelProvider(this).get(CampaignFeedViewModel.class);

        loadingIndicator.setVisibility(View.VISIBLE);
        viewModel.getCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            loadingIndicator.setVisibility(View.GONE);
            if (campaigns != null) {
                adapter.setCampaignList(campaigns);
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null && adapter != null) {
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
            String currentRole = prefs.getString("role", "SUPPORT");
            boolean isAdmin = currentRole.equalsIgnoreCase("ADMIN");
            adapter.setAdminStatus(isAdmin);
        }
    }
}