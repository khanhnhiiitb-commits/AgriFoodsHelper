package com.group5.agrifoodshelper.data.local;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.group5.agrifoodshelper.data.local.dao.FarmerReportDao;
import com.group5.agrifoodshelper.models.FarmerReport;

@Database(entities = {FarmerReport.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    // Migration 2 -> 3: thêm cột user_id cho farmer_reports (bỏ qua nếu cột đã tồn tại)
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

    // Khai báo các DAO (Các bạn khác sẽ vào code ruột)
    public abstract FarmerReportDao farmerReportDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "agrifoods_db")
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // các đường nâng cấp khác (vd 1 -> 3) sẽ xóa và tạo lại
                    .build();
        }
        return instance;
    }
}