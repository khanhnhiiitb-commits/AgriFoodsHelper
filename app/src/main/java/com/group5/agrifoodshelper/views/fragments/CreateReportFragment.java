package com.group5.agrifoodshelper.views.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.viewmodels.CreateReportViewModel;

public class CreateReportFragment extends Fragment {

    private ImageView imgCampaignPhoto;
    private Uri selectedImageUri = null;

    // Khai báo ViewModel
    private CreateReportViewModel createReportViewModel;

    // Bộ xử lý sự kiện chọn ảnh từ Gallery
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgCampaignPhoto.setImageURI(selectedImageUri); // Hiển thị ảnh vừa chọn lên màn hình
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel liên kết với vòng đời của Fragment
        createReportViewModel = new ViewModelProvider(this).get(CreateReportViewModel.class);

        // 1. Ánh xạ giao diện
        EditText edtCampaignTitle = view.findViewById(R.id.edtCampaignTitle);
        EditText edtCampaignQuantity = view.findViewById(R.id.edtCampaignQuantity);
        EditText edtCampaignAddress = view.findViewById(R.id.edtCampaignAddress);
        imgCampaignPhoto = view.findViewById(R.id.imgCampaignPhoto);
        MaterialButton btnPickImage = view.findViewById(R.id.btnPickImage);
        MaterialButton btnSubmitCampaign = view.findViewById(R.id.btnSubmitCampaign);

        // 2. Mở thư viện chọn ảnh
        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // 3. Lắng nghe thông báo trạng thái từ ViewModel để cập nhật giao diện
        createReportViewModel.getSaveStatus().observe(getViewLifecycleOwner(), status -> {
            Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();

            if (status.equals("Đã lưu bản nháp trên thiết bị")) {
                // Thoát về màn hình trước đó nếu lưu nháp thành công
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            } else {
                // Mở khóa lại nút bấm nếu có lỗi xảy ra
                btnSubmitCampaign.setEnabled(true);
                btnSubmitCampaign.setText("Gửi yêu cầu giải cứu");
            }
        });

        // 4. Gửi dữ liệu xuống ViewModel để xử lý lưu offline thay vì gọi online
        btnSubmitCampaign.setOnClickListener(v -> {
            String title = edtCampaignTitle.getText().toString().trim();
            String quantityStr = edtCampaignQuantity.getText().toString().trim();
            String address = edtCampaignAddress.getText().toString().trim();

            // Validate form
            if (title.isEmpty() || quantityStr.isEmpty() || address.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin bắt buộc (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Bắt buộc phải chọn ảnh
            if (selectedImageUri == null) {
                Toast.makeText(getContext(), "Vui lòng chọn hình ảnh nông sản", Toast.LENGTH_SHORT).show();
                return;
            }

            // Khóa nút bấm để tránh người dùng bấm nhiều lần
            btnSubmitCampaign.setEnabled(false);
            btnSubmitCampaign.setText("Đang lưu bản nháp...");

            // Gọi ViewModel truyền kèm URI dạng chuỗi. (SyncWorker sẽ xử lý upload ngầm sau)
            createReportViewModel.createReport(
                    title,
                    quantityStr,
                    selectedImageUri.toString(),
                    address
            );
        });
    }
}