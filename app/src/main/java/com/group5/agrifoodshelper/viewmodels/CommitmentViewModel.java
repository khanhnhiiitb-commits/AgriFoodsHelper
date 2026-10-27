package com.group5.agrifoodshelper.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.group5.agrifoodshelper.repositories.CommitmentRepository;

public class CommitmentViewModel extends ViewModel {
    private CommitmentRepository repository;
    private double currentPricePerKg = 15000.0;

    // LiveData để View (Activity) quan sát và hiển thị lên màn hình
    private MutableLiveData<String> successMessage = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Double> calculatedTotal = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public CommitmentViewModel() {
        repository = new CommitmentRepository();
    }

    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Double> getCalculatedTotal() { return calculatedTotal; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // Logic toán học: Tính tổng tiền dự tính khi người dùng nhập số kg
    public void calculateTotalPrice(String kgString) {
        try {
            double kg = Double.parseDouble(kgString.trim());
            if (kg > 0) {
                calculatedTotal.setValue(kg * currentPricePerKg);
            } else {
                calculatedTotal.setValue(0.0);
            }
        } catch (NumberFormatException e) {
            calculatedTotal.setValue(0.0);
        }
    }

    // Logic kiểm tra dữ liệu đầu vào và gửi yêu cầu góp đơn
    public void submitCommitment(String campaignId, String kgString) {
        if (kgString == null || kgString.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập số kg.");
            return;
        }

        double kg;
        try {
            kg = Double.parseDouble(kgString.trim());
        } catch (NumberFormatException e) {
            errorMessage.setValue("Số kg không hợp lệ.");
            return;
        }

        if (kg <= 0) {
            errorMessage.setValue("Số kg phải lớn hơn 0.");
            return;
        }

        isLoading.setValue(true);

        // Gọi Repository xử lý Firebase
        repository.addCommitment(campaignId, kg, new CommitmentRepository.OnCommitmentResult() {
            @Override
            public void onSuccess(String message) {
                isLoading.setValue(false);
                successMessage.setValue(message);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}