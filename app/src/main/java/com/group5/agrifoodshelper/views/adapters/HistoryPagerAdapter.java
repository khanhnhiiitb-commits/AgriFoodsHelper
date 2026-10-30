package com.group5.agrifoodshelper.views.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.group5.agrifoodshelper.views.fragments.HistoryTabContentFragment;

public class HistoryPagerAdapter extends FragmentStateAdapter {

    public HistoryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Truyền thẳng vị trí tab (0, 1, 2) vào chung 1 Fragment xử lý
        int safePosition = Math.max(0, Math.min(position, getItemCount() - 1));
        return HistoryTabContentFragment.newInstance(safePosition);
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}