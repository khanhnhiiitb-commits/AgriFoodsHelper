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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.viewmodels.JobBoardViewModel;
import com.group5.agrifoodshelper.views.adapters.JobAdapter;

import java.util.ArrayList;

public class JobBoardFragment extends Fragment {

    private RecyclerView recyclerViewJobBoard;
    private JobAdapter jobAdapter;
    private JobBoardViewModel viewModel;
    private ProgressBar loadingIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewJobBoard = view.findViewById(R.id.recyclerViewJobBoard);
        if (recyclerViewJobBoard != null) {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                recyclerViewJobBoard.setLayoutManager(new GridLayoutManager(getContext(), 2));
            } else {
                recyclerViewJobBoard.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        }
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // RESPONSIVE: Xoay ngang hiển thị 2 cột, dọc hiển thị 1 cột
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerViewJobBoard.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            recyclerViewJobBoard.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        jobAdapter = new JobAdapter();
        recyclerViewJobBoard.setAdapter(jobAdapter);

        // Khởi tạo đúng JobBoardViewModel
        viewModel = new ViewModelProvider(this).get(JobBoardViewModel.class);

        // Lắng nghe danh sách chuyến xe từ ViewModel (Sống sót qua việc xoay màn hình)
        viewModel.getJobList().observe(getViewLifecycleOwner(), jobBoardItems -> {
            if (jobBoardItems != null) {
                jobAdapter.setJobList(jobBoardItems);
            }
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });



        // Lắng nghe trạng thái Loading
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Lắng nghe thông báo lỗi hoặc trạng thái
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && getContext() != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Vuốt để làm mới danh sách chuyến xe
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> viewModel.loadJobs());
        }

        // Gọi tải dữ liệu chuyến xe lần đầu
        viewModel.loadJobs();
    }
}