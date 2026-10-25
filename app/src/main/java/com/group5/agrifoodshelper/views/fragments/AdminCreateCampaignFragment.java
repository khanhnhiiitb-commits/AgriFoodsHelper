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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.group5.agrifoodshelper.R;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminCreateCampaignFragment extends Fragment {

    private ImageView imgCampaignPhoto;
    private Uri selectedImageUri = null;
    private MaterialButton btnSubmit;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {

                            selectedImageUri = result.getData().getData();

                            if (selectedImageUri != null
                                    && imgCampaignPhoto != null) {
                                imgCampaignPhoto.setImageURI(selectedImageUri);
                            }
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_admin_create_campaign,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        EditText edtTitle =
                view.findViewById(R.id.edtCampaignTitle);

        EditText edtDetail =
                view.findViewById(R.id.edtCampaignDetail);

        EditText edtQuantity =
                view.findViewById(R.id.edtCampaignQuantity);

        EditText edtAddress =
                view.findViewById(R.id.edtCampaignAddress);

        imgCampaignPhoto =
                view.findViewById(R.id.imgCampaignPhoto);

        MaterialButton btnPickImage =
                view.findViewById(R.id.btnPickImage);

        btnSubmit =
                view.findViewById(R.id.btnSubmitCampaign);

        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                );

                imagePickerLauncher.launch(intent);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title =
                        edtTitle.getText().toString().trim();

                String detail =
                        edtDetail.getText().toString().trim();

                String quantityStr =
                        edtQuantity.getText().toString().trim();

                String address =
                        edtAddress.getText().toString().trim();

                if (title.isEmpty()
                        || detail.isEmpty()
                        || quantityStr.isEmpty()
                        || address.isEmpty()
                        || selectedImageUri == null) {

                    Toast.makeText(
                            requireContext(),
                            "Vui lòng nhập đủ thông tin và chọn ảnh",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                double targetQty;

                try {
                    targetQty = Double.parseDouble(quantityStr);

                    if (targetQty <= 0) {
                        edtQuantity.setError(
                                "Số lượng phải lớn hơn 0"
                        );
                        return;
                    }

                } catch (NumberFormatException e) {
                    edtQuantity.setError(
                            "Số lượng không hợp lệ"
                    );
                    return;
                }

                uploadCampaignToFirebase(
                        title,
                        detail,
                        targetQty,
                        address,
                        selectedImageUri,
                        btnSubmit
                );
            }
        });
    }

    private void uploadCampaignToFirebase(
            final String title,
            final String detail,
            final double targetQty,
            final String address,
            final Uri imageUri,
            final MaterialButton submitButton) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(
                    requireContext(),
                    "Vui lòng đăng nhập lại",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        final String currentUserId =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

        submitButton.setEnabled(false);
        submitButton.setText("Đang tải lên Firebase...");

        final String campaignId = UUID.randomUUID().toString();

        StorageReference storageRef =
                FirebaseStorage.getInstance()
                        .getReference()
                        .child("campaign_images/admin_" + campaignId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(
                        new OnSuccessListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(
                                    com.google.firebase.storage.UploadTask.TaskSnapshot taskSnapshot) {

                                storageRef.getDownloadUrl()
                                        .addOnSuccessListener(
                                                new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {

                                                        saveCampaign(
                                                                campaignId,
                                                                title,
                                                                detail,
                                                                targetQty,
                                                                address,
                                                                uri.toString(),
                                                                currentUserId,
                                                                submitButton
                                                        );
                                                    }
                                                }
                                        )
                                        .addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(
                                                            Exception e) {
                                                        showUploadError(
                                                                submitButton,
                                                                "Lỗi lấy đường dẫn ảnh: "
                                                                        + e.getMessage()
                                                        );
                                                    }
                                                }
                                        );
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                showUploadError(
                                        submitButton,
                                        "Lỗi tải ảnh: " + e.getMessage()
                                );
                            }
                        }
                );
    }

    private void saveCampaign(
            final String campaignId,
            final String title,
            final String detail,
            final double targetQty,
            final String address,
            final String imageUrl,
            final String currentUserId,
            final MaterialButton submitButton) {

        Map<String, Object> campaignData =
                new HashMap<String, Object>();

        campaignData.put("campaignId", campaignId);
        campaignData.put("title", title);
        campaignData.put("detail", detail);
        campaignData.put("targetQuantity", targetQty);
        campaignData.put("address", address);
        campaignData.put("imageUrl", imageUrl);
        campaignData.put("createdAt", System.currentTimeMillis());
        campaignData.put("isPinned", true);
        campaignData.put("currentQuantity", 0.0);
        campaignData.put("isActive", true);
        campaignData.put("status", "OPEN");
        campaignData.put("ownerId", currentUserId);
        campaignData.put("createdBy", "ADMIN");

        FirebaseFirestore.getInstance()
                .collection("Campaigns")
                .document(campaignId)
                .set(campaignData)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                if (!isAdded()) {
                                    return;
                                }

                                Toast.makeText(
                                        requireContext(),
                                        "Đăng chiến dịch khẩn cấp thành công!",
                                        Toast.LENGTH_LONG
                                ).show();

                                if (getActivity() != null) {
                                    getActivity().onBackPressed();
                                }
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                showUploadError(
                                        submitButton,
                                        "Lỗi lưu dữ liệu: " + e.getMessage()
                                );
                            }
                        }
                );
    }

    private void showUploadError(
            MaterialButton submitButton,
            String message) {

        if (!isAdded()) {
            return;
        }

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_LONG
        ).show();

        submitButton.setEnabled(true);
        submitButton.setText("Đăng chiến dịch khẩn cấp");
    }
}
