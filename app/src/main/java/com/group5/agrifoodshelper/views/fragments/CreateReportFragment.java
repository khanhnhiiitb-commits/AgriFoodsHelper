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

    private CreateReportViewModel createReportViewModel;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    com.bumptech.glide.Glide.with(requireContext())
                            .load(selectedImageUri)
                            .into(imgCampaignPhoto);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_report, container, false);
    }
    private String copyImageToInternalStorage(Uri uri) {
        try {
            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            // Tạo một file mới nằm an toàn trong thư mục nội bộ của app
            java.io.File file = new java.io.File(requireContext().getFilesDir(), "report_img_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            // Trả về đường dẫn thực tế (dạng file://...) để Worker thoải mái đọc offline
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createReportViewModel = new ViewModelProvider(this).get(CreateReportViewModel.class);

        EditText edtCampaignTitle = view.findViewById(R.id.edtCampaignTitle);
        EditText edtCampaignQuantity = view.findViewById(R.id.edtCampaignQuantity);
        EditText edtCampaignPrice = view.findViewById(R.id.edtCampaignPrice);
        EditText edtCampaignAddress = view.findViewById(R.id.edtCampaignAddress);
        imgCampaignPhoto = view.findViewById(R.id.imgCampaignPhoto);
        MaterialButton btnPickImage = view.findViewById(R.id.btnPickImage);
        MaterialButton btnSubmitCampaign = view.findViewById(R.id.btnSubmitCampaign);

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        createReportViewModel.getSaveStatus().observe(getViewLifecycleOwner(), status -> {
            Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();

            if (status.equals("Đã lưu bản nháp trên thiết bị")) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            } else {
                btnSubmitCampaign.setEnabled(true);
                btnSubmitCampaign.setText("Gửi yêu cầu giải cứu");
            }
        });

        btnSubmitCampaign.setOnClickListener(v -> {
            String title = edtCampaignTitle.getText().toString().trim();
            String quantityStr = edtCampaignQuantity.getText().toString().trim();
            String priceStr = edtCampaignPrice.getText().toString().trim();
            String address = edtCampaignAddress.getText().toString().trim();

            if (title.isEmpty() || quantityStr.isEmpty() || address.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin bắt buộc (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri == null) {
                Toast.makeText(getContext(), "Vui lòng chọn hình ảnh nông sản", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSubmitCampaign.setEnabled(false);
            btnSubmitCampaign.setText("Đang lưu bản nháp...");
            EditText edtCampaignDetail = view.findViewById(R.id.edtCampaignDetail);

            String detail = edtCampaignDetail.getText().toString().trim();
            String rescueTime = "";

            // 1. Gọi hàm copy ảnh bạn vừa tạo ở trên
            String internalUriString = copyImageToInternalStorage(selectedImageUri);

            if (internalUriString == null) {
                Toast.makeText(getContext(), "Lỗi xử lý ảnh, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                btnSubmitCampaign.setEnabled(true);
                btnSubmitCampaign.setText("Gửi yêu cầu giải cứu");
                return;
            }

            createReportViewModel.createReport(
                    title,
                    quantityStr,
                    priceStr,
                    internalUriString,
                    address,
                    detail,
                    rescueTime
            );

        });
    }
}