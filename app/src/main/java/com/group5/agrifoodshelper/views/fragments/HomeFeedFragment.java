package com.group5.agrifoodshelper.views.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Campaign;
import com.group5.agrifoodshelper.viewmodels.CampaignFeedViewModel;
import com.group5.agrifoodshelper.views.activities.CampaignDetailActivity;
import com.group5.agrifoodshelper.views.adapters.CampaignAdapter;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewFeed);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(CampaignFeedViewModel.class);

        // Responsive: Kiểm tra hướng màn hình để chia cột RecyclerView (Dọc: 1 cột, Ngang: 2 cột)
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        adapter = new CampaignAdapter();
        recyclerView.setAdapter(adapter);

        // Lắng nghe trạng thái dữ liệu từ ViewModel (Sống sót qua việc xoay màn hình)
        viewModel.getCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            if (campaigns != null) {
                adapter.setCampaignList(campaigns);
            }
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Lắng nghe trạng thái Loading
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Vuốt để làm mới
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> loadData());
        }

        // Sự kiện ghim bài viết
        adapter.setOnCampaignActionListener(this::loadData);

        // Sự kiện bấm vào item xem chi tiết
        adapter.setOnItemClickListener(campaign -> {
            if (getContext() != null) {
                android.content.Intent intent = new android.content.Intent(getContext(), CampaignDetailActivity.class);
                intent.putExtra("CAMPAIGN_ID", campaign.getCampaignId());
                startActivity(intent);
            }
        });

        // Gọi tải dữ liệu lần đầu tiên
        loadData();
    }

    private void loadData() {
        boolean hasNet = isNetworkAvailable(requireContext());
        if (!hasNet) {
            Toast.makeText(getContext(), "Đang hiển thị ở chế độ Ngoại tuyến", Toast.LENGTH_SHORT).show();
        }
        viewModel.fetchCampaigns(hasNet);
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null && adapter != null) {
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String currentRole = prefs.getString("role", "SUPPORT");
            boolean isAdmin = currentRole.equalsIgnoreCase("ADMIN");
            adapter.setAdminStatus(isAdmin);
        }
    }
}