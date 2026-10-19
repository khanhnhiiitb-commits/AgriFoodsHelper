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
            String address) {

        productName = productName.trim();
        quantityText = quantityText.trim();
        address = address.trim();

        if (productName.isEmpty()
                || quantityText.isEmpty()
                || address.isEmpty()) {
            saveStatus.setValue(
                    "Vui lòng nhập đầy đủ thông tin");
            return;
        }

        final String finalProductName = productName;
        final String finalQuantityText = quantityText;
        final String finalAddress = address;

        double quantity;

        try {
            quantity = Double.parseDouble(finalQuantityText);

            if (!Double.isFinite(quantity) || quantity <= 0) {
                saveStatus.setValue(
                        "Sản lượng phải lớn hơn 0");
                return;
            }
        } catch (NumberFormatException e) {
            saveStatus.setValue(
                    "Sản lượng không hợp lệ");
            return;
        }

        final double finalQuantity = quantity;

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
                        System.currentTimeMillis()
                );

                db.farmerReportDao().insert(report);

                // Yêu cầu đồng bộ khi có mạng.
                SyncWorker.scheduleSyncNow(getApplication());

                saveStatus.postValue(
                        "Đã lưu bản nháp trên thiết bị");

            } catch (Exception e) {
                saveStatus.postValue(
                        "Lưu bài đăng thất bại: "
                                + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
