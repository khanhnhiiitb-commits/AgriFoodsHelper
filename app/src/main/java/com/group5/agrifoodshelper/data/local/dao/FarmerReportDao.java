package com.group5.agrifoodshelper.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.agrifoodshelper.models.FarmerReport;

import java.util.List;

@Dao
public interface FarmerReportDao {

    @Insert
    void insert(FarmerReport report);

    @Query("SELECT * FROM farmer_reports WHERE isSynced = 0")
    List<FarmerReport> getUnsyncedReports();

    @Update
    void update(FarmerReport report);
}
