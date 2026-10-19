package com.group5.agrifoodshelper.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Campaign;
import com.group5.agrifoodshelper.viewmodels.CampaignFeedViewModel;
import com.group5.agrifoodshelper.views.adapters.JobAdapter;
import java.util.ArrayList;
import java.util.List;

public class JobBoardFragment extends Fragment {

    private RecyclerView recyclerViewJobBoard;
    private JobAdapter jobAdapter;
    private CampaignFeedViewModel viewModel;
    private List<Campaign> fullJobList = new ArrayList<>(); // Lưu trữ bản sao toàn bộ danh sách

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo RecyclerView và Adapter
        recyclerViewJobBoard = view.findViewById(R.id.recyclerViewJobBoard);
        recyclerViewJobBoard.setLayoutManager(new LinearLayoutManager(getContext()));

        jobAdapter = new JobAdapter();
        recyclerViewJobBoard.setAdapter(jobAdapter);

        // 2. Khởi tạo ViewModel và lắng nghe dữ liệu từ Firebase
        viewModel = new ViewModelProvider(this).get(CampaignFeedViewModel.class);

        viewModel.getCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            if (campaigns != null) {
                fullJobList = campaigns; // Lưu lại để dùng cho chức năng lọc
                jobAdapter.setJobList(campaigns); // Mặc định hiển thị "Tất cả"
            }
        });

        // 3. (Tùy chọn) Khởi tạo sự kiện cho các nút Lọc nếu bạn đã gán ID cho chúng trong XML
        // Ví dụ: view.findViewById(R.id.btnFilterDriver).setOnClickListener(v -> filterJobs("DRIVER"));
    }

    // Hàm logic để lọc công việc theo từ khóa
    private void filterJobs(String keyword) {
        List<Campaign> filteredList = new ArrayList<>();
        for (Campaign c : fullJobList) {
            // Giả định bạn có thuộc tính chứa từ khóa, ở đây lọc thử qua Tiêu đề
            if (c.getTitle() != null && c.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(c);
            }
        }
        jobAdapter.setJobList(filteredList);

        if(filteredList.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy công việc phù hợp", Toast.LENGTH_SHORT).show();
        }
    }
}