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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_feed, container, false);
    }

    private void loadCampaigns() {
        AppDatabase roomDb = AppDatabase.getInstance(requireContext());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (isNetworkAvailable(requireContext())) {
            // Tải toàn bộ danh sách bài viết từ Firebase mà không dùng orderBy để tránh bị mất dữ liệu cũ
            firestore.collection("Campaigns")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Campaign> onlineCampaigns = new java.util.ArrayList<>();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                Campaign c = doc.toObject(Campaign.class);
                                c.setCampaignId(doc.getId());
                                onlineCampaigns.add(c);
                            } catch (Exception e) {
                                android.util.Log.e("HomeFeedFragment", "Lỗi parse: " + e.getMessage());
                            }
                        }

                        // SẮP XẾP AN TOÀN TRONG JAVA: Bài nào isPinned = true sẽ tự động bay lên đầu tiên
                        java.util.Collections.sort(onlineCampaigns, (c1, c2) -> {
                            boolean p1 = c1 != null && c1.isPinned();
                            boolean p2 = c2 != null && c2.isPinned();
                            if (p1 != p2) {
                                return Boolean.compare(p2, p1);
                            }
                            long t1 = c1 != null ? c1.getCreatedAt() : 0;
                            long t2 = c2 != null ? c2.getCreatedAt() : 0;
                            return Long.compare(t2, t1);
                        });

                        // Cập nhật giao diện
                        if (adapter != null) {
                            adapter.setCampaignList(onlineCampaigns);
                        }
                        if (loadingIndicator != null) {
                            loadingIndicator.setVisibility(View.GONE);
                        }

                        // Tắt vòng xoay làm mới khi tải xong
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        // Lưu vào SQLite để đọc offline
                        new Thread(() -> {
                            roomDb.campaignDao().clearAll();
                            roomDb.campaignDao().insertCampaigns(onlineCampaigns);
                        }).start();
                    })
                    .addOnFailureListener(e -> {
                        if (loadingIndicator != null) {
                            loadingIndicator.setVisibility(View.GONE);
                        }
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        android.util.Log.e("HomeFeedFragment", "Lỗi tải Firebase: " + e.getMessage());
                    });
        } else {
            // Chế độ ngoại tuyến (Mất mạng)
            Toast.makeText(getContext(), "Đang hiển thị ở chế độ Ngoại tuyến", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                List<Campaign> offlineCampaigns = roomDb.campaignDao().getOfflineCampaigns();

                if (offlineCampaigns != null) {
                    java.util.Collections.sort(offlineCampaigns, (c1, c2) -> {
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

                requireActivity().runOnUiThread(() -> {
                    if (loadingIndicator != null) {
                        loadingIndicator.setVisibility(View.GONE);
                    }
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (adapter != null && offlineCampaigns != null) {
                        adapter.setCampaignList(offlineCampaigns);
                    }
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
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Cài đặt sự kiện vuốt để làm mới
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadCampaigns);
        }

        // KIỂM TRA HƯỚNG MÀN HÌNH ĐỂ RESPONSIVE
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        adapter = new CampaignAdapter();

        // LẮNG NGHE SỰ KIỆN GHIM TỪ ADAPTER ĐỂ TỰ ĐỘNG LOAD LẠI VÀ ĐẨY LÊN ĐẦU
        adapter.setOnCampaignActionListener(() -> {
            loadCampaigns();
        });

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
        loadCampaigns();
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