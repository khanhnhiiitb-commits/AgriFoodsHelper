package com.group5.agrifoodshelper.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.group5.agrifoodshelper.models.Campaign;
import com.group5.agrifoodshelper.repositories.CampaignRepository;
import java.util.List;

public class CampaignFeedViewModel extends ViewModel {
    private CampaignRepository repository;
    private LiveData<List<Campaign>> campaignListLiveData;
/*
    public CampaignFeedViewModel() {
        repository = new CampaignRepository();
        campaignListLiveData = repository.getActiveCampaigns();
    }
*/
    public LiveData<List<Campaign>> getCampaigns() {
        return campaignListLiveData;
    }
}