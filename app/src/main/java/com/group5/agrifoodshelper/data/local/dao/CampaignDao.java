package com.group5.agrifoodshelper.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.group5.agrifoodshelper.models.Campaign;
import java.util.List;

@Dao
public interface CampaignDao {

    // Lưu đè danh sách bài viết mới nhất lấy từ mây xuống SQLite
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCampaigns(List<Campaign> campaigns);

    // Lấy toàn bộ bài viết đã lưu để đọc Offline
    @Query("SELECT * FROM cached_campaigns WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    List<Campaign> getOfflineCampaigns();

    // Dọn dẹp bộ nhớ cũ
    @Query("DELETE FROM cached_campaigns")
    void clearAll();
}