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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.agrifoodshelper.R;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateReportFragment extends Fragment {

    private ImageView imgCampaignPhoto;
    private Uri selectedImageUri = null;

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

        // 3. Gửi dữ liệu
        // 3. Gửi dữ liệu (Upload ảnh -> Lấy URL -> Lưu Firestore)
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

            double targetQuantity = Double.parseDouble(quantityStr);

            // Khóa nút bấm để tránh người dùng bấm nhiều lần
            btnSubmitCampaign.setEnabled(false);
            btnSubmitCampaign.setText("Đang tải ảnh lên (0%)...");

            // Khởi tạo Firebase Storage
            com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
            // Tạo thư mục "campaign_images" trên mây và đặt tên file ngẫu nhiên
            com.google.firebase.storage.StorageReference storageRef = storage.getReference()
                    .child("campaign_images/" + UUID.randomUUID().toString() + ".jpg");

            // Tiến hành upload file ảnh
            storageRef.putFile(selectedImageUri)
                    .addOnProgressListener(snapshot -> {
                        // Hiển thị % tải ảnh cho chuyên nghiệp
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        btnSubmitCampaign.setText(String.format("Đang tải ảnh lên (%.0f%%)...", progress));
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        // Tải ảnh thành công, tiến hành lấy URL thật (Download URL)
                        btnSubmitCampaign.setText("Đang lưu dữ liệu...");
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String realImageUrl = uri.toString(); // Đây chính là link https://...

                            // Gọi hàm lưu dữ liệu lên Firestore và truyền URL này vào
                            saveCampaignToFirestore(title, targetQuantity, address, realImageUrl, btnSubmitCampaign);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSubmitCampaign.setEnabled(true);
                        btnSubmitCampaign.setText("Gửi yêu cầu giải cứu");
                    });
        });
    }

    // Thêm tham số String imageUrl vào hàm
    private void saveCampaignToFirestore(String title, double targetQuantity, String address, String imageUrl, MaterialButton btnSubmit) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "UNKNOWN";
        String campaignId = UUID.randomUUID().toString();

        Map<String, Object> campaignData = new HashMap<>();
        campaignData.put("campaignId", campaignId);
        campaignData.put("title", title);
        campaignData.put("targetQuantity", targetQuantity);
        campaignData.put("currentQuantity", 0.0);
        campaignData.put("address", address);
        campaignData.put("authorId", userId);
        campaignData.put("status", "PENDING");
        campaignData.put("createdAt", System.currentTimeMillis());
        campaignData.put("imageUrl", imageUrl);

        db.collection("Campaigns").document(campaignId)
                .set(campaignData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đăng bài thành công! Đang chờ Admin duyệt.", Toast.LENGTH_LONG).show();
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Gửi yêu cầu giải cứu");
                });
    }
}