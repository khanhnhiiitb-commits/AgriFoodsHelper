package com.group5.agrifoodshelper.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.group5.agrifoodshelper.models.Commitment;
import com.group5.agrifoodshelper.repositories.CommitmentRepository;

import java.util.UUID;

public class CommitmentViewModel extends ViewModel {

    private final CommitmentRepository repository;
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public CommitmentViewModel() {
        this.repository = new CommitmentRepository();
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public double calculateEstimatedPrice(double quantityKg, double unitPrice) {
        if (quantityKg <= 0 || unitPrice <= 0) return 0.0;
        return quantityKg * unitPrice;
    }

    public void submitCommitment(String campaignId, String supporterId, String inputQuantityStr) {
        if (inputQuantityStr == null || inputQuantityStr.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập số kg bạn muốn góp mua.");
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(inputQuantityStr.trim());
        } catch (NumberFormatException e) {
            errorMessage.setValue("Số lượng nhập vào không hợp lệ!");
            return;
        }

        if (quantity <= 0) {
            errorMessage.setValue("Số lượng góp mua phải lớn hơn 0 kg.");
            return;
        }

        if (quantity > 10000) {
            errorMessage.setValue("Vượt hạn mức tối đa 10,000 kg.");
            return;
        }

        String commitmentId = UUID.randomUUID().toString();
        long currentTimestamp = System.currentTimeMillis();

        Commitment commitment = new Commitment(
                commitmentId,
                campaignId,
                supporterId,
                quantity,
                currentTimestamp
        );

        isLoading.setValue(true);

        repository.submitCommitmentWithIncrement(commitment, new CommitmentRepository.OnCommitmentCallback() {
            @Override
            public void onSuccess(String message) {
                isLoading.postValue(false);
                successMessage.postValue(message);
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }
}