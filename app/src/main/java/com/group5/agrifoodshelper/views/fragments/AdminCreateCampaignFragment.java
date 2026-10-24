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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Campaign;

import java.util.UUID;

public class AdminCreateCampaignFragment extends Fragment {

    private ImageView imgCampaignPhoto;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgCampaignPhoto.setImageURI(selectedImageUri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_create_campaign, container, false);
        // Hãy đảm bảo bạn có layout fragment_admin_create_campaign.xml tương ứng
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText edtTitle = view.findViewById(R.id.edtCampaignTitle);
        EditText edtDetail = view.findViewById(R.id.edtCampaignDetail);
        EditText edtQuantity = view.findViewById(R.id.edtCampaignQuantity);
        EditText edtAddress = view.findViewById(R.id.edtCampaignAddress);
        imgCampaignPhoto = view.findViewById(R.id.imgCampaignPhoto);
        MaterialButton btnPickImage = view.findViewById(R.id.btnPickImage);
        MaterialButton btnSubmit = view.findViewById(R.id.btnSubmitCampaign);

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String detail = edtDetail.getText().toString().trim();
            String quantityStr = edtQuantity.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();

            if (title.isEmpty() || detail.isEmpty() || quantityStr.isEmpty() || address.isEmpty() || selectedImageUri == null) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin và chọn ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadCampaignToFirebase(title, detail, Double.parseDouble(quantityStr), address, selectedImageUri, btnSubmit);
        });
    }

    private void uploadCampaignToFirebase(String title, String detail, double targetQty, String address, Uri imageUri, MaterialButton btnSubmit) {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang tải lên Firebase...");

        String campaignId = UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("campaign_images/admin_" + campaignId + ".jpg");

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();

                Campaign campaign = new Campaign(
                        campaignId, title, detail, targetQty, address,
                        imageUrl, System.currentTimeMillis(), true
                );

                FirebaseFirestore.getInstance().collection("Campaigns").document(campaignId)
                        .set(campaign)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Đăng chiến dịch khẩn cấp thành công!", Toast.LENGTH_LONG).show();
                            if (getActivity() != null) getActivity().onBackPressed();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show();
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Đăng chiến dịch khẩn cấp");
                        });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi tải ảnh", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            btnSubmit.setText("Đăng chiến dịch khẩn cấp");
        });
    }
}