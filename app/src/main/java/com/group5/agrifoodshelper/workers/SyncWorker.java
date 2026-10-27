package com.group5.agrifoodshelper.workers;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.group5.agrifoodshelper.data.local.AppDatabase;
import com.group5.agrifoodshelper.models.FarmerReport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        try {
            List<FarmerReport> reports = db.farmerReportDao().getUnsyncedReports();

            if (reports.isEmpty()) {
                return Result.success();
            }

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseStorage storage = FirebaseStorage.getInstance();

            for (FarmerReport report : reports) {
                String downloadUrl = "";

                // 1. Upload ảnh (nếu có)
                if (report.getLocalImageUri() != null && !report.getLocalImageUri().isEmpty()) {
                    Uri imageUri = Uri.parse(report.getLocalImageUri());
                    StorageReference storageRef = storage.getReference()
                            .child("farmer_images/" + report.getReportId() + ".jpg");

                    Tasks.await(storageRef.putFile(imageUri));
                    Uri downloadUri = Tasks.await(storageRef.getDownloadUrl());
                    downloadUrl = downloadUri.toString();
                }

                // 2. Chủ đơn: ưu tiên userId đã lưu lúc tạo đơn,
                //    chỉ fallback sang user đang đăng nhập khi đơn cũ chưa có userId
                String ownerId = report.getUserId();
                if (ownerId == null || ownerId.isEmpty()) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        ownerId = currentUser.getUid();
                        report.setUserId(ownerId);
                    }
                }

                // 3. Dữ liệu đẩy lên Firestore
                Map<String, Object> data = new HashMap<>();
                data.put("reportId", report.getReportId());
                data.put("userId", ownerId);   // phục vụ duyệt đơn và lịch sử
                data.put("ownerId", ownerId);  // giữ lại theo bản của nhóm
                data.put("createdBy", "FARMER");
                data.put("status", "PENDING");
                data.put("isActive", true);
                data.put("title", report.getTitle());
                data.put("productName", report.getProductName());
                data.put("estimatedQuantity", report.getEstimatedQuantity());
                data.put("price", report.getPrice());
                data.put("locationData", report.getLocationData());
                data.put("createdAt", report.getCreatedAt());
                data.put("imageUrl", downloadUrl);

                // Trường phục vụ kiểm duyệt
                data.put("detail", report.getDetail());
                data.put("rescueTime", report.getRescueTime());

                // TUYỆT ĐỐI CHỈ GHI VÀO farmer_reports
                Tasks.await(
                        firestore.collection("farmer_reports")
                                .document(report.getReportId())
                                .set(data)
                );

                report.setSynced(true);
                db.farmerReportDao().update(report);
            }

            showNotification("Đồng bộ thành công", "Yêu cầu giải cứu của bạn đã được gửi đến Admin.");
            return Result.success();

        } catch (Exception e) {
            return Result.retry();
        }
    }

    @SuppressLint("MissingPermission")
    private void showNotification(String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "sync_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Đồng bộ dữ liệu", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    public static void schedulePeriodicSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "farmer_report_sync", ExistingPeriodicWorkPolicy.KEEP, request);
    }

    public static void scheduleSyncNow(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints).build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                "farmer_report_sync_now", ExistingWorkPolicy.KEEP, request);
    }
}