package com.group5.agrifoodshelper.data.local;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.group5.agrifoodshelper.data.local.dao.CampaignDao;
import com.group5.agrifoodshelper.data.local.dao.FarmerReportDao;
import com.group5.agrifoodshelper.models.Campaign;
import com.group5.agrifoodshelper.models.FarmerReport;

// 1. Thêm Campaign.class vào entities và tăng version lên 4
@Database(entities = {FarmerReport.class, Campaign.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    // Migration 2 -> 3: thêm cột user_id cho farmer_reports (giữ nguyên của bạn)
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            boolean hasUserId = false;
            Cursor cursor = db.query("PRAGMA table_info(farmer_reports)");
            try {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    if ("user_id".equals(cursor.getString(nameIndex))) {
                        hasUserId = true;
                        break;
                    }
                }
            } finally {
                cursor.close();
            }
            if (!hasUserId) {
                db.execSQL("ALTER TABLE farmer_reports ADD COLUMN user_id TEXT");
            }
        }
    };

    // Khai báo các DAO
    public abstract FarmerReportDao farmerReportDao();

    // 2. Thêm DAO cho bảng tin Offline
    public abstract CampaignDao campaignDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "agrifoods_db")
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // Tự động dọn dẹp và tạo bảng mới khi version tăng lên 4
                    .build();
        }
        return instance;
    }
}