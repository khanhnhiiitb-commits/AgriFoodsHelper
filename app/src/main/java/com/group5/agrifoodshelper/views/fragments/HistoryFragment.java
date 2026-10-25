package com.group5.agrifoodshelper.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.views.adapters.HistoryPagerAdapter;

public class HistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate file giao diện fragment_history.xml chứa TabLayout và ViewPager2
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tabLayoutHistory);
        ViewPager2 viewPager = view.findViewById(R.id.viewPagerHistory);

        // Khởi tạo Adapter điều phối 3 tab
        HistoryPagerAdapter pagerAdapter = new HistoryPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Đồng bộ TabLayout với ViewPager2 và đặt tiêu đề cho từng tab
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Chiến dịch của tôi");
                    break;
                case 1:
                    tab.setText("Việc đã nhận");
                    break;
                case 2:
                    tab.setText("Đơn đã góp");
                    break;
            }
        }).attach();
    }
}