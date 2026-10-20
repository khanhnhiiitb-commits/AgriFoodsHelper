package com.group5.agrifoodshelper.views.fragments;

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
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.viewmodels.CampaignFeedViewModel;
import com.group5.agrifoodshelper.views.activities.CampaignDetailActivity;
import com.group5.agrifoodshelper.views.adapters.CampaignAdapter;

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
                android.content.Intent intent = new android.content.Intent(getContext(), com.group5.agrifoodshelper.views.activities.CampaignDetailActivity.class);
                intent.putExtra("CAMPAIGN_ID", campaign.getCampaignId());
                startActivity(intent);
            }
        });

        viewModel = new ViewModelProvider(this).get(CampaignFeedViewModel.class);

        viewModel = new ViewModelProvider(this).get(CampaignFeedViewModel.class);

        loadingIndicator.setVisibility(View.VISIBLE);
        viewModel.getCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            loadingIndicator.setVisibility(View.GONE);
            if (campaigns != null) {
                adapter.setCampaignList(campaigns);
            }
        });
    }
}
