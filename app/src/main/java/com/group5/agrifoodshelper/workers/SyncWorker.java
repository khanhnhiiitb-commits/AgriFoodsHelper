package com.group5.agrifoodshelper.workers;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.group5.agrifoodshelper.data.local.AppDatabase;
import com.group5.agrifoodshelper.models.FarmerReport;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncWorker extends Worker {

    public SyncWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        try {
            List<FarmerReport> reports =
                    db.farmerReportDao().getUnsyncedReports();

            FirebaseFirestore firestore =
                    FirebaseFirestore.getInstance();

            for (FarmerReport report : reports) {

                Map<String, Object> data = new HashMap<>();
                data.put("reportId", report.getReportId());
                data.put("title", report.getTitle());
                data.put("productName", report.getProductName());
                data.put("estimatedQuantity",
                        report.getEstimatedQuantity());
                data.put("locationData", report.getLocationData());
                data.put("createdAt", report.getCreatedAt());

                // Gửi bài đăng lên Firestore.
                Tasks.await(
                        firestore.collection("farmer_reports")
                                .document(report.getReportId())
                                .set(data)
                );

                // Chỉ đánh dấu đã đồng bộ sau khi gửi thành công.
                report.setSynced(true);
                db.farmerReportDao().update(report);
            }

            return Result.success();

        } catch (Exception e) {
            // Nếu có lỗi mạng hoặc lỗi đồng bộ,
            // WorkManager sẽ thử lại.
            return Result.retry();
        }
    }

    public static void schedulePeriodicSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        SyncWorker.class,
                        15,
                        TimeUnit.MINUTES
                )
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "farmer_report_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    public static void scheduleSyncNow(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(SyncWorker.class)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                "farmer_report_sync_now",
                ExistingWorkPolicy.KEEP,
                request
        );
    }

}
