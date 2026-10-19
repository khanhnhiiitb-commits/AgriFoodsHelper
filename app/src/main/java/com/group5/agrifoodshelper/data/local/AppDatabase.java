package com.group5.agrifoodshelper.data.local;


import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.group5.agrifoodshelper.data.local.dao.FarmerReportDao;
import com.group5.agrifoodshelper.models.FarmerReport;

@Database(entities = {FarmerReport.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    // Khai báo các DAO (Các bạn khác sẽ vào code ruột)
     public abstract FarmerReportDao farmerReportDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "agrifoods_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
