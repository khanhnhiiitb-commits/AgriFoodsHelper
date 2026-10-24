package com.group5.agrifoodshelper.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

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

        String finalProductName = productName;
        String finalAddress = address;

        executor.execute(() -> {
            try {
                String reportId = UUID.randomUUID().toString();

                JSONObject location = new JSONObject();
                location.put("address", finalAddress);

                FarmerReport report = new FarmerReport(
                        reportId,
                        "Giải cứu " + finalProductName,
                        finalProductName,
                        finalQuantity,
                        location.toString(),
                        false,
                        System.currentTimeMillis(),
                        localImageUri,
                        "PENDING",      // Trạng thái
                        detail,         // Chi tiết
                        rescueTime      // Thời gian giải cứu
                );

                db.farmerReportDao().insert(report);
                SyncWorker.scheduleSyncNow(getApplication());
                saveStatus.postValue("Đã lưu bản nháp trên thiết bị");

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
