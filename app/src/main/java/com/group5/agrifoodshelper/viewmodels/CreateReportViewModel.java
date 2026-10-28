package com.group5.agrifoodshelper.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.group5.agrifoodshelper.data.local.AppDatabase;
import com.group5.agrifoodshelper.models.FarmerReport;
import com.group5.agrifoodshelper.workers.SyncWorker;

import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateReportViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executor;

    private final MutableLiveData<String> saveStatus =
            new MutableLiveData<>();

    public CreateReportViewModel(@NonNull Application application) {
        super(application);

        db = AppDatabase.getInstance(application);
        executor = Executors.newSingleThreadExecutor();
    }

    public MutableLiveData<String> getSaveStatus() {
        return saveStatus;
    }

    public void createReport(
            String productName,
            String quantityText,
            String priceText,
            String localImageUri,
            String address,
            String detail,
            String rescueTime) {

        if (productName.isEmpty() || quantityText.isEmpty() || address.isEmpty()) {
            saveStatus.postValue("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        double finalQuantity;
        try {
            finalQuantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            saveStatus.postValue("Số lượng không hợp lệ");
            return;
        }
        double finalPrice;
        try {
            finalPrice = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            saveStatus.postValue("Giá bán không hợp lệ");
            return;
        }
        String finalProductName = productName;
        String finalAddress = address;

        // Lấy UID của user đang đăng nhập hiện tại
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        executor.execute(() -> {
            try {
                String reportId = UUID.randomUUID().toString();

                String locationString = finalAddress;


                        // Thay vì dùng constructor dài dễ bị lệch tham số, ta dùng constructor rỗng + setter:
                FarmerReport report = new FarmerReport();
                report.setReportId(reportId);
                report.setTitle("Giải cứu " + finalProductName);
                report.setProductName(finalProductName);
                report.setEstimatedQuantity(finalQuantity);
                report.setPrice(finalPrice);
                report.setLocationData(locationString);
                report.setSynced(false);
                report.setCreatedAt(System.currentTimeMillis());
                report.setLocalImageUri(localImageUri);
                report.setStatus("PENDING");
                report.setDetail(detail);
                report.setRescueTime(rescueTime);
                report.setUserId(currentUserId); // Gán trực tiếp userId vào đây


                // Gán userId vào report
                report.setUserId(currentUserId);

                db.farmerReportDao().insert(report);
                saveStatus.postValue("Đã lưu bản nháp trên thiết bị");
                SyncWorker.scheduleSyncNow(getApplication());
                saveStatus.postValue("Đã gưỉ yêu cầu đăng bài giải cứu");

            } catch (Exception e) {
                saveStatus.postValue("Lưu bài đăng thất bại: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}